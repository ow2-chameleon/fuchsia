package org.ow2.chameleon.fuchsia.importer.push;

import org.apache.felix.ipojo.annotations.*;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.osgi.framework.ServiceReference;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.http.HttpService;
import org.ow2.chameleon.fuchsia.core.component.AbstractImporterComponent;
import org.ow2.chameleon.fuchsia.core.declaration.ImportDeclaration;
import org.ow2.chameleon.fuchsia.core.exceptions.BinderException;
import org.ow2.chameleon.fuchsia.push.base.subscriber.SubscriberOutput;
import org.ow2.chameleon.fuchsia.push.base.subscriber.servlet.CallbackServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.*;

@Component
@Provides
public class SubscriptionImporter extends AbstractImporterComponent implements SubscriberOutput {

    private static final Logger LOG = LoggerFactory.getLogger(SubscriptionImporter.class);

    static List<String> approvedActions = new Vector<String>();

    private List<String> callbacksRegistered=new ArrayList<String>();


    @ServiceProperty(name = "target", value = "(push.hub.url=*)")
    private String filter;

    @Requires
    HttpService httpService;

    @Requires
    EventAdmin eventAdmin;

    private ServiceReference serviceReference;

    @PostRegistration
    public void registration(ServiceReference serviceReference) {
        this.serviceReference = serviceReference;
    }

	@Validate
    public void start() {
        LOG.info("PuSH importer started.");
        super.start();
	}

    @Invalidate
    public void stop(){
        LOG.info("PuSH importer stopped.");
        super.stop();
    }

    public String getName() {
        return this.getClass().getSimpleName();
    }

    public int subscribe(String hub, String topic_url,String hostname,String verifytoken,String lease_seconds) throws Exception {
        if (topic_url != null) {

            String callbackserverurl= hostname;

            HttpPost httppost = new HttpPost(hub);
            List<NameValuePair> nvps = new ArrayList<NameValuePair>();
            nvps.add(new BasicNameValuePair("hub.callback", callbackserverurl));
            nvps.add(new BasicNameValuePair("hub.mode", "subscribe"));
            nvps.add(new BasicNameValuePair("hub.topic", topic_url));
            nvps.add(new BasicNameValuePair("hub.verify", "sync"));
            if (lease_seconds != null)
                nvps.add(new BasicNameValuePair("hub.lease_seconds", lease_seconds));
            //For future https implementation
            //if ((secret !=null) && (secret.getBytes("utf8").length < 200))
            //	nvps.add(new BasicNameValuePair("hub.hub.secret", secret));
            if (verifytoken !=null)
                nvps.add(new BasicNameValuePair("hub.verify_token", verifytoken));

            addAction("subscribe", topic_url, verifytoken);

            httppost.setEntity(new UrlEncodedFormEntity(nvps));
            httppost.setHeader("Content-type", "application/x-www-form-urlencoded");
            httppost.setHeader("User-agent", "RSS pubsubhubbub 0.3");

            CloseableHttpClient httpclient = HttpClients.createDefault();

            HttpResponse response=httpclient.execute(httppost);

            if (response != null){
                return response.getStatusLine().getStatusCode();
            } else {
                return 400;
            }
        }
        return 400;
    }

    public int unsubscribe(String hub, String topic_url,String hostname,String verifytoken) throws Exception {
        if (topic_url != null) {

            String callbackserverurl= hostname;

            HttpPost httppost = new HttpPost(hub);
            List<NameValuePair> nvps = new ArrayList<NameValuePair>();
            nvps.add(new BasicNameValuePair("hub.callback", callbackserverurl));
            nvps.add(new BasicNameValuePair("hub.mode", "unsubscribe"));
            nvps.add(new BasicNameValuePair("hub.topic", topic_url));
            nvps.add(new BasicNameValuePair("hub.verify", "sync"));
            //For future https implementation
            //if ((secret !=null) && (secret.getBytes("utf8").length < 200))
            //	nvps.add(new BasicNameValuePair("hub.hub.secret", secret));
            if (verifytoken !=null)
                nvps.add(new BasicNameValuePair("hub.verify_token", verifytoken));

            addAction("unsubscribe", topic_url, verifytoken);

            httppost.setEntity(new UrlEncodedFormEntity(nvps));

            httppost.setHeader("Content-type", "application/x-www-form-urlencoded");
            httppost.setHeader("User-agent", "ERGO RSS pubsubhubbub 0.3");

            CloseableHttpClient httpclient = HttpClients.createDefault();
            HttpResponse response=httpclient.execute(httppost);

            if (response != null){
                return response.getStatusLine().getStatusCode();
            } else {
                return 400;
            }
        }
        return 400;
    }

    private void addAction(String hubmode, String hubtopic, String hubverify) {
        String action=hubmode + ":" + hubtopic + ":" + hubverify;
        getApprovedActions().add(action);
    }

    public List<String> getApprovedActions() {
        return approvedActions;
    }

    @Override
    protected void useImportDeclaration(ImportDeclaration importDeclaration) throws BinderException {

        LOG.info("adding import declaration {}", importDeclaration);

        try {

            Map<String,Object> data=importDeclaration.getMetadata();

            String hub = data.get("push.hub.url").toString();
            String hub_topic = data.get("push.hub.topic").toString();
            String callback=data.get("push.subscriber.callback").toString();

            URI callbackURI=new URI(callback);

            httpService.registerServlet(callbackURI.getPath(),new CallbackServlet(eventAdmin,importDeclaration,this),null,null);

            int statusCode = subscribe(hub, hub_topic,callback, null, null);

            if (statusCode == 204){
                LOG.info("the status code of the subscription is 204: the request was verified and that the subscription is active");
            } else if (statusCode == 202){
                LOG.info("the status code of the subscription is 202: the subscription has yet to be verified (asynchronous verification)");
            } else{
                LOG.info("the status code of the subscription is {}", statusCode);
            }

            callbacksRegistered.add(callback);

            importDeclaration.handle(serviceReference);
        } catch (Exception e) {
            LOG.error("failed to import declaration, with the message: " + e.getMessage(), e);
        }

    }

    @Override
    protected void denyImportDeclaration(ImportDeclaration importDeclaration) throws BinderException {

        LOG.info("removing import declaration {}", importDeclaration);

        Map<String,Object> data=importDeclaration.getMetadata();
        String hub = data.get("push.hub.url").toString();//"http://localhost:8080/subscribe";
        String hub_topic = data.get("push.hub.topic").toString();//"http://blogname.blogspot.com/feeds/posts/default";
        String targetCallback=data.get("push.subscriber.callback").toString();

        importDeclaration.unhandle(serviceReference);

        for(String callback:callbacksRegistered){

            if(callback.equals(targetCallback)){

                LOG.info("Removing callback {}", callback);

                httpService.unregister(callback);

                try {

                    unsubscribe(hub, hub_topic, targetCallback, null);

                    LOG.info("Callback {} removed from the subscriber", callback);

                } catch (Exception e) {
                    LOG.error("Callback " + callback + " failed to be removed from the subscriber with the message", e);
                }

                break;
            }

        }

    }

    @Override
    protected Logger getLogger() {
        return LOG;
    }
}
