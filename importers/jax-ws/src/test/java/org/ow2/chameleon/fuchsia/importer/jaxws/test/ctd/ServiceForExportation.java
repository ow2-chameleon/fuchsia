package org.ow2.chameleon.fuchsia.importer.jaxws.test.ctd;

import org.osgi.framework.ServiceReference;

/**
 * CrashTestDummie interface for the test
 */
public interface ServiceForExportation extends ServiceReference {

    public void ping();

    public void ping(String value);

    public void ping(Integer value);

    public String pongString(String input);

    public Integer pongInteger(Integer input);

}
