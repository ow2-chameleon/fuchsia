package org.ow2.chameleon.fuchsia.jsonrpc.importer.it;

import org.jabsorb.JSONRPCServlet;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.http.HttpService;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

import java.util.Dictionary;
import java.util.Hashtable;

import static org.junit.Assert.fail;


public class JSONRPCServerActivator implements BundleActivator, ServiceTrackerCustomizer {
    private static final String SERVLET_NAME = "/JSONRPC";
    ServiceTracker httpTracker;
    private BundleContext context;

    public void start(BundleContext pContext) throws Exception {
        context = pContext;
        httpTracker = new ServiceTracker(context, HttpService.class.getName(), this);
        httpTracker.open();
    }

    public void stop(BundleContext context) throws Exception {
        httpTracker.close();
    }

    public Object addingService(ServiceReference reference) {
        HttpService httpservice = (HttpService) context.getService(reference);
        Dictionary<String, String> props = new Hashtable<String, String>();
        props.put("gzip_threshold", "200");
        try {
            httpservice.registerServlet(SERVLET_NAME, new JSONRPCServlet(), props, null);
        } catch (Exception e) {
            fail(e.getMessage());
        }

        return httpservice;
    }

    public void modifiedService(ServiceReference reference, Object service) {
    }

    public void removedService(ServiceReference reference, Object service) {
        HttpService httpservice = (HttpService) service;
        httpservice.unregister(SERVLET_NAME);

        context.ungetService(reference);
    }

}