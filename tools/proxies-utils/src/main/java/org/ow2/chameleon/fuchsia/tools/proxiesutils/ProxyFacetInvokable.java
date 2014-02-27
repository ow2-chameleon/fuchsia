package org.ow2.chameleon.fuchsia.tools.proxiesutils;

public interface ProxyFacetInvokable extends FuchsiaProxy  {

    /**
     * Sync
     *
     * @param method
     * @param args
     * @return
     */
    Object invoke(String method, Object... args) throws ProxyInvokationException;

    /**
     * Async
     *
     * @param method
     * @param transactionID
     * @param callback
     * @param args
     */
    void invoke(String method, Integer transactionID, Object callback, Object... args) throws ProxyInvokationException;

}
