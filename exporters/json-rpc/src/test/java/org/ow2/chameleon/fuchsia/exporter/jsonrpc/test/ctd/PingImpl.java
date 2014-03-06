package org.ow2.chameleon.fuchsia.exporter.jsonrpc.test.ctd;

import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceReference;

public class PingImpl implements Ping,ServiceReference {

    public void ping(){
        System.out.println("hello ping");
    }

    public Object getProperty(String key) {
        return null;
    }

    public String[] getPropertyKeys() {
        return new String[0];
    }

    public Bundle getBundle() {
        return null;
    }

    public Bundle[] getUsingBundles() {
        return new Bundle[0];
    }

    public boolean isAssignableTo(Bundle bundle, String className) {
        return false;
    }

    public int compareTo(Object reference) {
        return 0;
    }
}
