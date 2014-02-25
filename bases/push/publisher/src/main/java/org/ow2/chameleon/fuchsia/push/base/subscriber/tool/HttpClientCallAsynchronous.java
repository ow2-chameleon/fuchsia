package org.ow2.chameleon.fuchsia.push.base.subscriber.tool;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpClientCallAsynchronous extends Thread {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpClientCallAsynchronous.class);

    private final HttpClient httpClient;
    private final HttpContext context;
    private final HttpPost httppost;

    private HttpResponse httpresponse = null;


    public HttpClientCallAsynchronous(HttpClient httpClient, HttpPost httppost) {
        this.httpClient = httpClient;
        this.context = new BasicHttpContext();
        this.httppost = httppost;
    }

    @Override
    public void run() {
        try {
            httpresponse = this.httpClient.execute(this.httppost, this.context);
            HttpEntity entity = httpresponse.getEntity();
            if (entity != null) {
                entity.getContent().close();
            }
        } catch (Exception ex) {
            LOGGER.error("Failed to create assynchronous response", ex);
            this.httppost.abort();
        }
    }
}
