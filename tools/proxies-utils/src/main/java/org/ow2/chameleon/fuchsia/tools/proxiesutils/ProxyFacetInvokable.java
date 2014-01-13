package org.ow2.chameleon.fuchsia.tools.proxiesutils;

public interface ProxyFacetInvokable extends FuchsiaProxy  {

    /**
     * Sync
     *
     * @param method
     * @param args
     * @return
     */
    public Object invoke(String method, Object... args);

    /**
     * Async
     *
     * @param method
     * @param transactionID
     * @param callback
     * @param args
     */
    public void invoke(String method, Integer transactionID, Object callback, Object... args);

}
