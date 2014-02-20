package org.ow2.chameleon.fuchsia.importer.jaxws.test.ctd;

import org.osgi.framework.Bundle;

/**
 * CrashTestDummie class for the test
 */
public class ServiceForExportationimpl implements ServiceForExportation {

    public void ping() {
        System.out.println("ping received");
    }

    public void ping(String value) {
        System.out.println("ping string "+value+" received");
    }

    public void ping(Integer value) {
        System.out.println("ping int "+value+" received");
    }

    public String pongString(String input) {
        return input;
    }

    public Integer pongInteger(Integer input) {
        return input;
    }

    public Object getProperty(String key) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String[] getPropertyKeys() {
        return new String[0];  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Bundle getBundle() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Bundle[] getUsingBundles() {
        return new Bundle[0];  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean isAssignableTo(Bundle bundle, String className) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public int compareTo(Object reference) {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

}
