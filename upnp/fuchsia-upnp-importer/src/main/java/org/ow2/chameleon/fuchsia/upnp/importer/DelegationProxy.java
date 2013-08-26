package org.ow2.chameleon.fuchsia.upnp.importer;

import org.ow2.chameleon.fuchsia.fake.device.GenericDevice;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * This class is a proxy in order to delegate calls on the fake device
 * <strong>Note : In the future this will use events !</strong>
 *
 * @author jeremy.savonet@gmail.com
 */
public class DelegationProxy implements InvocationHandler {

    private final GenericDevice m_proxied;

    public DelegationProxy(GenericDevice proxied) {
        this.m_proxied = proxied;
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        return method.invoke(m_proxied, args);
    }
}
