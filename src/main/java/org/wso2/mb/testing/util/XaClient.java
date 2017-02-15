package org.wso2.mb.testing.util;

import javax.jms.JMSException;
import javax.jms.Session;
import javax.jms.XAConnection;
import javax.jms.XAConnectionFactory;
import javax.jms.XASession;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.transaction.xa.XAResource;

public class XaClient {

    private XAConnection xaConnection;
    private XAResource xaResource;
    private Session session;

    public XaClient(XAConnection xaConnection, XAResource xaResource, Session session) {
        this.xaConnection = xaConnection;
        this.xaResource = xaResource;
        this.session = session;
    }

    /**
     * Getter for xaResource
     */
    public XAResource getXaResource() {
        return xaResource;
    }

    /**
     * Getter for session
     */
    public Session getSession() {
        return session;
    }

    public static XaClient createNewClient(InitialContext initialContext) throws NamingException, JMSException {
        XAConnectionFactory connectionFactory = (XAConnectionFactory) initialContext
                .lookup(JMSClientHelper.QUEUE_CONNECTION_FACTORY);

        XAConnection xaConnection = connectionFactory.createXAConnection();
        XASession xaSession = xaConnection.createXASession();

        XAResource xaResource = xaSession.getXAResource();
        Session session = xaSession.getSession();

        return new XaClient(xaConnection, xaResource, session);
    }

    public void close() throws JMSException {
        session.close();
        xaConnection.close();
    }
}
