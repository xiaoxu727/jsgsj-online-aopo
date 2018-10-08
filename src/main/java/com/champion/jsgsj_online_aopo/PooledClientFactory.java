package com.champion.jsgsj_online_aopo;

import org.apache.commons.pool.PoolableObjectFactory;
import org.apache.commons.pool.impl.GenericObjectPool;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;

public class PooledClientFactory {
    private final static PooledClientFactory instance = new PooledClientFactory();
    private final GenericObjectPool clientPool = new GenericObjectPool();

    @SuppressWarnings("deprecation")
    public PooledClientFactory() {
	clientPool.setFactory(new PoolableObjectFactory() {
	    @Override
	    public boolean validateObject(Object arg0) {
		return false;
	    }

	    @Override
	    public void passivateObject(Object arg0) throws Exception {
	    }

	    @Override
	    public Object makeObject() throws Exception {
		WebClient client = new WebClient(BrowserVersion.CHROME);
		// client.getOptions().setUseInsecureSSL(true);
		client.getOptions().setTimeout(30000);
		client.getOptions().setCssEnabled(false);
		client.getOptions().setJavaScriptEnabled(false);
		client.getOptions().setThrowExceptionOnScriptError(false);
		client.getOptions().setThrowExceptionOnFailingStatusCode(false);
		client.getCookieManager().setCookiesEnabled(true);
		return client;
	    }

	    @Override
	    public void destroyObject(Object arg0) throws Exception {
		WebClient client = (WebClient) arg0;
		client.close();
		client = null;
	    }

	    @Override
	    public void activateObject(Object arg0) throws Exception {
	    }
	});
    }

    public static PooledClientFactory getInstance() {
	return instance;
    }

    public WebClient getClient() throws Exception {
	return (WebClient) this.clientPool.borrowObject();
    }

    public void returnClient(WebClient client) throws Exception {
	this.clientPool.returnObject(client);
    }

    public static PooledClientFactory getInstance(int maxActive) {
	instance.clientPool.setMaxActive(maxActive);
	return instance;
    }
}