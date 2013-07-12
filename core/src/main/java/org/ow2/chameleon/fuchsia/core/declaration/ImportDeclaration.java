package org.ow2.chameleon.fuchsia.core.declaration;

import org.ow2.chameleon.fuchsia.core.component.DiscoveryService;
import org.ow2.chameleon.fuchsia.core.component.ImporterService;

import java.util.Map;

/**
 * {@link ImportDeclaration} is a data transfer object (DTO pattern) that transit between layers
 * in Fuchsia.
 * They are created by the Fuchsia {@link DiscoveryService}
 *
 * @author Morgan Martinet
 */
public interface ImportDeclaration {

    String IMPORTATION_PROTOCOL = "fuchsia.importation.protocol";
    String IMPORTATION_PROTOCOL_NAME = IMPORTATION_PROTOCOL + ".name";
    String IMPORTATION_PROTOCOL_VERSION = IMPORTATION_PROTOCOL + ".version";

    Map<String, Object> getMetadata();

    Map<String, Object> getExtraMetadata();

    Status getStatus();

    void bind(ImporterService importerService);

    void unbind(ImporterService importerService);

}
