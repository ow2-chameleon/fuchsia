package org.ow2.chameleon.fuchsia.push.base.hub.dto;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.ow2.chameleon.fuchsia.push.base.hub.exception.InvalidContentNotification;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

public class SubscriptionRequest {

    String callback;
    String mode;
    String topic;
    String verify;

    public SubscriptionRequest(String callback,String mode, String topic, String verify){

        this.callback=callback;
        this.mode=mode;
        this.topic=topic;
        this.verify=verify;

    }

    public static SubscriptionRequest from(HttpServletRequest request) throws InvalidContentNotification {

        validateRequest(request);

        SubscriptionRequest sr=new SubscriptionRequest(
                request.getParameter("hub.callback"),
                request.getParameter("hub.mode"),
                request.getParameter("hub.topic"),
                request.getParameter("hub.verify")
                );

        return sr;

    }

    public List<NameValuePair> toRequestParameters(){

        List <NameValuePair> nvps = new ArrayList<NameValuePair>();
        nvps.add(new BasicNameValuePair("hub.callback", getCallback()));
        nvps.add(new BasicNameValuePair("hub.mode", getMode()));
        nvps.add(new BasicNameValuePair("hub.topic", getTopic()));
        nvps.add(new BasicNameValuePair("hub.verify", getVerify()));

        return nvps;

    }

    private static void validateRequest(HttpServletRequest request) throws InvalidContentNotification {

        if(
                request.getParameter("hub.callback")==null ||
                request.getParameter("hub.mode")==null ||
                request.getParameter("hub.topic")==null ||
                request.getParameter("hub.verify")==null
         ){
            throw new InvalidContentNotification("Parameters requested not defined in the request");
        }
    }

    public String getCallback() {
        return callback;
    }

    public String getMode() {
        return mode;
    }

    public String getTopic() {
        return topic;
    }

    public String getVerify() {
        return verify;
    }
}
