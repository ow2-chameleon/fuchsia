package org.ow2.chameleon.fuchsia.jsonrpc.importer;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.jabsorb.client.ClientError;
import org.jabsorb.client.HTTPSession;
import org.jabsorb.client.Session;
import org.jabsorb.client.TransportRegistry;
import org.jabsorb.client.TransportRegistry.SessionFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

public class ImporterHTTPSession implements Session {

    protected final HttpClient client;

    protected URI uri;

    public ImporterHTTPSession(URI uri) {
        this.uri = uri;
        client = new DefaultHttpClient();
    }

    /**
     * As per JSON-RPC Working Draft
     * http://json-rpc.org/wd/JSON-RPC-1-1-WD-20060807.html#RequestHeaders
     */
    static final String JSON_CONTENT_TYPE = "application/json";

    public JSONObject sendAndReceive(JSONObject message) {
        try {
            HttpPost postMethod = new HttpPost(uri.toString());
            postMethod.setHeader("Content-Type", JSON_CONTENT_TYPE);
            postMethod.setEntity(new StringEntity(message.toString()));

            HttpResponse response = client.execute(postMethod);

            if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                throw new ClientError("HTTP Status - "
                        + response.getStatusLine().toString());
            }

            HttpEntity entity = response.getEntity();
            JSONTokener tokener = new JSONTokener(asString(entity.getContent()));
            Object rawResponseMessage = tokener.nextValue();
            JSONObject responseMessage = (JSONObject) rawResponseMessage;
            if (responseMessage == null)
                throw new ClientError("Invalid response type - "
                        + rawResponseMessage.getClass());

            return responseMessage;
        } catch (ClientProtocolException ce) {
            throw new ClientError(ce);
        } catch (IOException e) {
            e.printStackTrace();
            throw new ClientError(e);
        } catch (JSONException e) {
            e.printStackTrace();
            throw new ClientError(e);
        }
    }

    private String asString(InputStream content) throws IOException {
        BufferedInputStream is = new BufferedInputStream(content);
        ByteArrayOutputStream os = new ByteArrayOutputStream(4096);
        int count;
        byte[] buffer = new byte[4096];
        while ((count = is.read(buffer, 0, 4096)) > 0) {
            os.write(buffer, 0, count);
        }

        os.flush();

        return os.toString();
    }

    static class Factory implements SessionFactory {
        public Session newSession(URI uri) {
            return new HTTPSession(uri);
        }
    }

    /**
     * Register this transport in 'registry'
     */
    public static void register(TransportRegistry registry) {
        registry.registerTransport("http", new Factory());
    }

    public void close() {
        client.getConnectionManager().shutdown();
    }

}
