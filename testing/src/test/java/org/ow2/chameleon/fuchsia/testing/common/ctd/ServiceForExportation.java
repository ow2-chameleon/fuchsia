package org.ow2.chameleon.fuchsia.testing.common.ctd;

import org.osgi.framework.ServiceReference;

public interface ServiceForExportation extends ServiceReference {

    public void ping();

    public void ping(String value);

    public void ping(Integer value);

    public String pongString(String input);

    public Integer pongInteger(Integer input);



}
