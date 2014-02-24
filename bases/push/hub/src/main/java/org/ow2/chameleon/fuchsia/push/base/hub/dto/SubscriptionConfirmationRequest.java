package org.ow2.chameleon.fuchsia.push.base.hub.dto;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.ow2.chameleon.fuchsia.push.base.hub.exception.InvalidContentNotification;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

import static org.ow2.chameleon.fuchsia.push.base.hub.HubConstants.*;

/**
 * Created with IntelliJ IDEA.
 * User: jnascimento
 * Date: 26/09/13
 * Time: 14:49
 * To change this template use File | Settings | File Templates.
 */
public class SubscriptionConfirmationRequest {


    private final String mode;
    private final String topic;
    private final String challenge;
    private final String lease;

    public SubscriptionConfirmationRequest(String mode, String topic, String challenge, String lease) {

        this.mode = mode;
        this.topic = topic;
        this.challenge = challenge;
        this.lease = lease;

    }

    public static SubscriptionConfirmationRequest from(HttpServletRequest request) throws InvalidContentNotification {

        validateRequest(request);

        return new SubscriptionConfirmationRequest(
                request.getParameter(HUB_MODE),
                request.getParameter(HUB_TOPIC),
                request.getParameter(HUB_CHALLENGE),
                request.getParameter(HUB_LEASE_SECONDS)
        );

    }

    private static void validateRequest(HttpServletRequest request) throws InvalidContentNotification {

        if (request.getParameter(HUB_MODE) == null ||
                request.getParameter(HUB_TOPIC) == null ||
                request.getParameter(HUB_CHALLENGE) == null ||
                request.getParameter(HUB_LEASE_SECONDS) == null) {
            throw new InvalidContentNotification("No enough information provided");
        }
    }

    public List<NameValuePair> toRequestParameters() {

        List<NameValuePair> nvps = new ArrayList<NameValuePair>();
        nvps.add(new BasicNameValuePair(HUB_MODE, getMode()));
        nvps.add(new BasicNameValuePair(HUB_TOPIC, getTopic()));
        nvps.add(new BasicNameValuePair(HUB_CHALLENGE, getChallenge()));
        nvps.add(new BasicNameValuePair(HUB_LEASE_SECONDS, getLease()));

        return nvps;

    }

    public String getMode() {
        return mode;
    }

    public String getTopic() {
        return topic;
    }

    public String getChallenge() {
        return challenge;
    }

    public String getLease() {
        return lease;
    }

}
