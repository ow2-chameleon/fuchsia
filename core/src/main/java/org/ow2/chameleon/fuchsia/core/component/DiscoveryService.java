package org.ow2.chameleon.fuchsia.core.component;

/**
 * The components providing this service are capable of discovering local services or devices from a
 * protocol/technology/... and to reifying them into {@link org.ow2.chameleon.fuchsia.core.declaration.ImportDeclaration} which will be publish as a service on
 * the OSGi platform.
 *
 * @author Morgan Martinet
 */
public interface DiscoveryService {

    String getName();

}
