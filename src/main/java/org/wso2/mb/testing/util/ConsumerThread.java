package org.wso2.mb.testing.util;

import org.apache.log4j.Logger;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

public class ConsumerThread extends Thread{
    /**
     * Class logger
     */
    private static final Logger LOGGER = Logger.getLogger(ProducerThread.class);
    private final XaClient consumerClient;
    private final Session session;
    private final XAResource xaResource;
    private final Xid xid;
    private final MessageConsumer consumer;
    private final int iterations;

    public ConsumerThread(Queue queue, XaClient consumerClient, int iterations) throws JMSException {
        this.iterations = iterations;
        this.consumerClient = consumerClient;
        session = consumerClient.getSession();
        xaResource = consumerClient.getXaResource();

        xid = JMSClientHelper.getNewXid();
        consumer = session.createConsumer(queue);
    }

    public void run() {
        TpsCalculator tpsCalculator = new TpsCalculator();
        tpsCalculator.start();

        try {

            for (int i = 0; i < iterations; i++) {
                xaResource.start(xid, XAResource.TMNOFLAGS);
                Message receive = consumer.receive();
                tpsCalculator.mark();
                xaResource.end(xid, XAResource.TMSUCCESS);

                int ret = xaResource.prepare(xid);

                if (ret == XAResource.XA_OK) {
                    xaResource.commit(xid, false);
                } else {
                    throw new Exception("Prepare failed");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            tpsCalculator.stop();
            try {
                consumerClient.close();
            } catch (JMSException e) {
                LOGGER.error("Error while closing producer client", e);
            }
        }

        LOGGER.info("Consumer TPS: " + tpsCalculator.getTps());

    }

    public static Thread createConsumer(InitialContext initialContext, String queueName, int iterations)
            throws JMSException, NamingException {
        XaClient xaClient = XaClient.createNewClient(initialContext);
        Queue destination = (Queue) initialContext.lookup(queueName);
        return new ConsumerThread(destination, xaClient, iterations);
    }
}
