package org.ow2.chameleon.fuchsia.push.base.hub.dto;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.ow2.chameleon.fuchsia.push.base.hub.exception.InvalidContentNotification;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

public class ContentNotification {

    private String mode;
    private String url;

    public ContentNotification(String mode,String url){
        this.mode=mode;
        this.url=url;
    }

    public static ContentNotification from(HttpServletRequest request) throws InvalidContentNotification {

        validateRequest(request);

        ContentNotification cn=new ContentNotification(
                request.getParameter("hub.mode"),
                request.getParameter("hub.url")
        );



        return cn;
    }

    public List<NameValuePair> toRequesParameters(){

        List <NameValuePair> nvps = new ArrayList<NameValuePair>();
        nvps.add(new BasicNameValuePair("hub.mode", getMode()));
        nvps.add(new BasicNameValuePair("hub.url", getUrl()));

        return nvps;

    }

    public String getMode() {
        return mode;
    }

    public String getUrl() {
        return url;
    }

    private static void validateRequest(HttpServletRequest request) throws InvalidContentNotification {

        if(!request.getContentType().equals("application/x-www-form-urlencoded")){
            throw new InvalidContentNotification("Invalid content type");
        }

        if(request.getParameter("hub.mode")==null){
            throw new InvalidContentNotification("No hub.mode provided");
        }

        if(request.getParameter("hub.url")==null){
            throw new InvalidContentNotification("No hub.url provided");
        }
    }
}

