package org.wso2.mb.testing.util;

import org.apache.log4j.Logger;

import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

public class ProducerThread extends Thread {

    /**
     * Class logger
     */
    private static final Logger LOGGER = Logger.getLogger(ProducerThread.class);

    private final Session session;
    private final XAResource xaResource;
    private final TextMessage textMessage;
    private final XaClient producerClient;
    private final Xid xid;
    private final int iterations;
    private MessageProducer producer;

    private ProducerThread(Queue queue, XaClient producerClient, int messageSize, int iterations) throws
            JMSException {
        this.producerClient = producerClient;
        this.iterations = iterations;
        session = producerClient.getSession();
        xaResource = producerClient.getXaResource();

        String content = JMSClientHelper.getContent(messageSize);
        textMessage = session.createTextMessage(content);

        xid = JMSClientHelper.getNewXid();
        producer = session.createProducer(queue);
    }

    private static Thread createProducerWithQueue(Queue queue, XaClient producerClient,
            int messageSize, int iterations)
            throws JMSException {
        ProducerThread producerThread = new ProducerThread(queue, producerClient, messageSize, iterations);
        producerThread.session.createQueue(queue.getQueueName());
        return producerThread;
    }

    public void run() {
        TpsCalculator tpsCalculator = new TpsCalculator();
        tpsCalculator.start();

        try {
            for (int i = 0; i < iterations; i++) {
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
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            tpsCalculator.stop();
            try {
                producerClient.close();
            } catch (JMSException e) {
                LOGGER.error("Error while closing producer client", e);
            }
        }

        LOGGER.info("Publisher TPS: " + tpsCalculator.getTps());
    }

    public static Thread createProducerWithQueue(InitialContext initialContext, String queueName, int messageSize,
            int iterations)
            throws JMSException, NamingException {
        XaClient xaClient = XaClient.createNewClient(initialContext);
        Queue destination = (Queue) initialContext.lookup(queueName);
        return createProducerWithQueue(destination, xaClient, messageSize, iterations);
    }
}
