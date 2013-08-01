package org.ow2.chameleon.fuchsia.importer;

import org.ow2.chameleon.fuchsia.device.GenericDevice;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * This class is a proxy in order to delegate calls on the fake device
 * Note : In the future this will use events !
 *
 * @author jeremy.savonet@gmail.com
 */
public class DelegationProxy implements InvocationHandler {

    private final GenericDevice m_proxied;

    public DelegationProxy(GenericDevice proxied) {
        this.m_proxied = proxied;
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
//
//        if(method.getName().startsWith("getSerial"))
//            return "test";

        return method.invoke(m_proxied, args);
    }
}
