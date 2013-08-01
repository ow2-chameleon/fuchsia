/**
 *
 *   Copyright 2011-2012 Universite Joseph Fourier, LIG, ADELE Research
 *   Group Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package org.ow2.chameleon.fuchsia.fake.tests;

import org.apache.felix.ipojo.*;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.ow2.chameleon.testing.helpers.OSGiHelper;

import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Dictionary;

public class ICasaHelper {
    private final BundleContext context;

    public ICasaHelper(BundleContext pContext) {
        context = pContext;
    }

    /**
     * Create an instance with the given factory and properties
     * @param factory
     * @param properties
     * @return
     */
    public static ComponentInstance createInstance(final Factory factory, final Dictionary<String, Object> properties) {
        ComponentInstance instance = null;

        // Create an instance
        try {
            instance = factory.createComponentInstance(properties);
        } catch (UnacceptableConfiguration e) {
        } catch (MissingHandlerException e) {
        } catch (ConfigurationException e) {
        }

        return instance;
    }

    /**
     * Get the Factory linked to the given pid
     * @param osgi
     * @param name
     * @return The facory
     */
    public static Factory getValidFactory(final OSGiHelper osgi, final String name) {
        // Get The Factory ServiceReference
        ServiceReference facref = osgi.getServiceReference(Factory.class.getName(), "(&(factory.state=1)(factory.name=" + name + "))");
        // Get the factory
        Factory factory = (Factory) osgi.getServiceObject(facref);

        return factory;
    }

    public static void doPut(String gwurl, String descjson) throws Exception {
        // Send data
        URL url = new URL(gwurl);
        HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
        httpCon.setDoOutput(true);
        httpCon.setRequestProperty("Content-Type", "json");
        httpCon.setRequestMethod("PUT");
        OutputStreamWriter out = new OutputStreamWriter(httpCon.getOutputStream());
        out.write(descjson);
        out.close();
        if (httpCon.getResponseCode() != HttpURLConnection.HTTP_NO_CONTENT) {
            throw new Exception("Error Http Code: " + httpCon.getResponseCode());
        }
        httpCon.disconnect();
    }

    public static void doDelete(final String gwurl) throws Exception {
        // Send data
        URL url = new URL(gwurl);
        HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
        httpCon.setDoOutput(false);
        httpCon.setRequestMethod("DELETE");
        httpCon.connect();
        if (httpCon.getResponseCode() != HttpURLConnection.HTTP_NO_CONTENT) {
            throw new Exception("Error Http Code: " + httpCon.getResponseCode());
        }
        httpCon.disconnect();
    }

    public static void waitForIt(int time){
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            assert false;
        }
    }

    public <T> ServiceRegistration registerService(T service,Class<T> klass){
        return context.registerService(klass.getName(), service, null);
    }

    @SuppressWarnings("unchecked")
    public <T> T getServiceObject(Class<T> klass, String filter) {
        ServiceReference[] srefs = null;

        try {
            srefs = context.getAllServiceReferences(klass.getName(), filter);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        if (srefs != null && srefs.length > 0) {
            return (T) context.getService(srefs[0]);
        }
        else {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T getServiceObject(Class<T> klass) {
        ServiceReference sref = null;

        sref = context.getServiceReference(klass.getName());

        if (sref != null) {
            T service = (T) context.getService(sref);
            context.ungetService(sref);
            return service;
        } else {
            return null;
        }
    }
}
