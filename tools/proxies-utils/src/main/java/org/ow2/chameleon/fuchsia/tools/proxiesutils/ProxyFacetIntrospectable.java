package org.ow2.chameleon.fuchsia.tools.proxiesutils;

public interface ProxyFacetIntrospectable extends FuchsiaProxy {
    public <T> T get(String varName, Class<T> klass);
}
