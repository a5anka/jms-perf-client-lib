/*
 * Copyright (c) 2017, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.mb.testing.util;

import com.google.common.primitives.Longs;
import org.apache.commons.lang.StringUtils;

import java.util.Properties;
import java.util.concurrent.atomic.AtomicLong;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.transaction.xa.Xid;

/**
 * Util class with common helper methods when writing client code
 */
public class JMSClientHelper {
    private final static String ONE_KB_STRING = "abcdefghijklmnopqrstuvwxyz0123456789\n"
            + "abcdefghijklmnopqrstuvwxyz0123456789\n"
            + "abcdefghijklmnopqrstuvwxyz0123456789\n"
            + "abcdefghijklmnopqrstuvwxyz0123456789\n"
            + "abcdefghijklmnopqrstuvwxyz0123456789\n"
            + "abcdefghijklmnopqrstuvwxyz0123456789\n"
            + "abcdefghijklmnopqrstuvwxyz0123456789\n"
            + "abcdefghijklmnopqrstuvwxyz0123456789\n"
            + "abcdefghijklmnopqrstuvwxyz0123456789\n"
            + "abcdefghijklmnopqrstuvwxyz0123456789\n"
            + "abcdefghijklmnopqrstuvwxyz0123456789\n"
            + "abcdefghijklmnopqrstuvwxyz0123456789\n"
            + "abcdefghijklmnopqrstuvwxyz0123456789\n"
            + "abcdefghijklmnopqrstuvwxyz0123456789\n"
            + "abcdefghijklmnopqrstuvwxyz0123456789\n"
            + "abcdefghijklmnopqrstuvwxyz0123456789\n"
            + "abcdefghijklmnopqrstuvwxyz0123456789\n"
            + "abcdefghijklmnopqrstuvwxyz0123456789\n"
            + "abcdefghijklmnopqrstuvwxyz0123456789\n"
            + "abcdefghijklmnopqrstuvwxyz0123456789\n"
            + "abcdefghijklmnopqrstuvwxyz0123456789\n"
            + "abcdefghijklmnopqrstuvwxyz0123456789\n"
            + "abcdefghijklmnopqrstuvwxyz0123456789\n"
            + "abcdefghijklmnopqrstuvwxyz0123456789\n"
            + "abcdefghijklmnopqrstuvwxyz0123456789\n"
            + "abcdefghijklmnopqrstuvwxyz0123456789\n"
            + "abcdefghijklmnopqrstuvwxyz0123456789\n"
            + "abcdefghijklmnopqrstuvwxy";

    /**
     * Queue connection factory name used
     */
    public static final String QUEUE_CONNECTION_FACTORY = "andesQueueConnectionfactory";

    /**
     * Topic connection factory name used
     */
    static final String TOPIC_CONNECTION_FACTORY = "andesTopicConnectionfactory";

    public static AtomicLong GLOBAL_ID_GENERATOR =  new AtomicLong();

    /**
     * Return a different Xid each time this method is invoked
     *
     * @return Xid
     */
    public static Xid getNewXid() {
        return new TestXidImpl(100, Longs.toByteArray(GLOBAL_ID_GENERATOR.incrementAndGet()), new byte[] { 0x01 });
    }

    public static InitialContextBuilder createInitialContextBuilder(String username, String password, String brokerHost,
            int brokerPort) {
        return new InitialContextBuilder(username, password, brokerHost, brokerPort);
    }

    /**
     * Return a String with size matching the give size in KB
     *
     * @param sizeInKB size of the String in KB
     * @return content as String
     */
    public static String getContent(int sizeInKB) {
        return StringUtils.repeat(ONE_KB_STRING, sizeInKB);
    }

    public static class InitialContextBuilder {
        /**
         * Full qualified class name of the andes initial context factory
         */
        static final String ANDES_INITIAL_CONTEXT_FACTORY = "org.wso2.andes.jndi.PropertiesFileInitialContextFactory";

        private final String username;
        private final String password;
        private final String brokerHost;
        private final int brokerPort;
        private final Properties contextProperties;

        InitialContextBuilder(String username, String password, String brokerHost, int brokerPort) {
            this.username = username;
            this.password = password;
            this.brokerHost = brokerHost;
            this.brokerPort = brokerPort;

            contextProperties = new Properties();
            contextProperties.put(Context.INITIAL_CONTEXT_FACTORY, ANDES_INITIAL_CONTEXT_FACTORY);
        }

        public InitialContextBuilder withQueue(String queueName) {
            contextProperties.put("queue." + queueName, queueName);
            return this;
        }

        public InitialContext build() throws NamingException {
            String connectionString = getBrokerConnectionString(username, password, brokerHost, brokerPort);
            contextProperties.put("connectionfactory." + QUEUE_CONNECTION_FACTORY, connectionString);
            contextProperties.put("connectionfactory." + TOPIC_CONNECTION_FACTORY, connectionString);
            return new InitialContext(contextProperties);
        }

        private String getBrokerConnectionString(String username, String password, String brokerHost, int brokerPort) {
            return "amqp://" + username + ":" + password + "@clientID/carbon?brokerlist='tcp://"
                    + brokerHost + ":" + brokerPort + "'";
        }
    }
}
