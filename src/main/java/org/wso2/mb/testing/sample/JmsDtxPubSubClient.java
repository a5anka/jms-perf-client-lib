package org.wso2.mb.testing.sample;

import org.apache.log4j.Logger;
import org.wso2.mb.testing.util.ConsumerThread;
import org.wso2.mb.testing.util.JMSClientHelper;
import org.wso2.mb.testing.util.ProducerThread;

import javax.naming.InitialContext;

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
        int iterations = 100000;

        InitialContext initialContext = JMSClientHelper.createInitialContextBuilder("admin",
                                                                                    "admin",
                                                                                    "localhost",
                                                                                    5672)
                                                       .withQueue(queueName).build();


        Thread producerThread = ProducerThread.createProducerWithQueue(initialContext, queueName, 1, iterations);
        producerThread.start();

        Thread consumerThread = ConsumerThread.createConsumer(initialContext, queueName, iterations);
        consumerThread.start();

        producerThread.join();
        consumerThread.join();

        LOGGER.info("Test completed");
    }

}
