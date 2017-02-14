package org.wso2.mb.testing;

import org.apache.log4j.Logger;

import javax.jms.Destination;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.XAConnection;
import javax.jms.XAConnectionFactory;
import javax.jms.XASession;
import javax.naming.InitialContext;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

public class JmsDtxPubSubClient {

    /**
     * Class logger
     */
    private static final Logger LOGGER = Logger.getLogger(JmsDtxPubSubClient.class);

    /**
     * Main method
     *
     * @param args arguments
     */
    public static void main(String[] args) throws Exception {

        String queueName = "TestQueue";
        InitialContext initialContext = JMSClientHelper.createInitialContextBuilder("admin",
                                                                                    "admin",
                                                                                    "localhost",
                                                                                    5672)
                                                       .withQueue(queueName).build();

        String content = JMSClientHelper.getContent(1);

        XAConnectionFactory connectionFactory = (XAConnectionFactory) initialContext
                .lookup(JMSClientHelper.QUEUE_CONNECTION_FACTORY);

        XAConnection xaConnection = connectionFactory.createXAConnection();
        XASession xaSession = xaConnection.createXASession();

        XAResource xaResource = xaSession.getXAResource();
        Session session = xaSession.getSession();

        Destination xaTestQueue = (Destination) initialContext.lookup(queueName);
        session.createQueue(queueName);
        MessageProducer producer = session.createProducer(xaTestQueue);

        TextMessage textMessage = session.createTextMessage(content);
        Xid xid = JMSClientHelper.getNewXid();

        TpsCalculator tpsCalculator = new TpsCalculator();

        tpsCalculator.start();

        for (int i = 0; i < 2000; i++) {
            xaResource.start(xid, XAResource.TMNOFLAGS);
            producer.send(textMessage);
            tpsCalculator.mark();
            xaResource.end(xid, XAResource.TMSUCCESS);

            int ret = xaResource.prepare(xid);

            if (ret == XAResource.XA_OK) {
                xaResource.commit(xid, false);
            } else {
                throw new Exception("Prepare failed");
            }
        }

        tpsCalculator.stop();

        session.close();
        xaConnection.close();

        LOGGER.info("Publisher TPS: " + tpsCalculator.getTps());
    }
}
