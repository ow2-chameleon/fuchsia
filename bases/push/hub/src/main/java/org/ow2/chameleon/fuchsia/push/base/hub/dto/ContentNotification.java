package org.ow2.chameleon.fuchsia.push.base.hub.dto;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.ow2.chameleon.fuchsia.push.base.hub.exception.InvalidContentNotification;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

import static org.ow2.chameleon.fuchsia.push.base.hub.HubConstants.*;

public class ContentNotification {

    private String mode;
    private String url;

    public ContentNotification(String mode, String url) {
        this.mode = mode;
        this.url = url;
    }

    public static ContentNotification from(HttpServletRequest request) throws InvalidContentNotification {

        validateRequest(request);

        ContentNotification cn = new ContentNotification(
                request.getParameter(HUB_MODE),
                request.getParameter(HUB_URL)
        );


        return cn;
    }

    public List<NameValuePair> toRequesParameters() {

        List<NameValuePair> nvps = new ArrayList<NameValuePair>();
        nvps.add(new BasicNameValuePair(HUB_MODE, getMode()));
        nvps.add(new BasicNameValuePair(HUB_URL, getUrl()));

        return nvps;

    }

    public String getMode() {
        return mode;
    }

    public String getUrl() {
        return url;
    }

    private static void validateRequest(HttpServletRequest request) throws InvalidContentNotification {

        if (!"application/x-www-form-urlencoded".equals(request.getContentType())) {
            throw new InvalidContentNotification("Invalid content type");
        }

        if (request.getParameter(HUB_MODE) == null) {
            throw new InvalidContentNotification("No " + HUB_MODE + " provided");
        }

        if (request.getParameter(HUB_URL) == null) {
            throw new InvalidContentNotification("No " + HUB_URL + " provided");
        }
    }
}

