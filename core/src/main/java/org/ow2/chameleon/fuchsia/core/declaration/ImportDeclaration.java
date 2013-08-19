package org.ow2.chameleon.fuchsia.core.declaration;

import org.ow2.chameleon.fuchsia.core.component.DiscoveryService;
import org.ow2.chameleon.fuchsia.core.component.ImporterService;

import java.util.Map;

/**
 * {@link ImportDeclaration} is a data transfer object (DTO pattern) that transit between layers
 * in Fuchsia.
 * They are created by the {@link DiscoveryService}
 *
 * @author Morgan Martinet
 */
public interface ImportDeclaration {

    String IMPORTATION_PROTOCOL = "fuchsia.importation.protocol";
    String IMPORTATION_PROTOCOL_NAME = IMPORTATION_PROTOCOL + ".name";
    String IMPORTATION_PROTOCOL_VERSION = IMPORTATION_PROTOCOL + ".version";

    /**
     * @return the metadata of the ImportDeclaration
     */
    Map<String, Object> getMetadata();

    /**
     * @return the extra-metadata of the ImportDeclaration
     */
    Map<String, Object> getExtraMetadata();

    /**
     * Return the Status of the ImportDeclaration at the moment t of the method call.
     * The status contains the bindings which exist between the ImportDeclaration and
     * the ImporterServices.
     *
     * @return the actual (at this moment t) ImportDeclaration status.
     */
    Status getStatus();

    /**
     * Bind the given ImporterService to the ImportDeclaration.
     * This method stock the ImporterService to remember the binding.
     * <p/>
     * This method should only be called by a ImportationLinker.
     * The linker must call this method when it give the ImportDeclaration to the ImporterService.
     *
     * @param importerService the ImporterService the ImportDeclaration is bind to.
     */
    void bind(ImporterService importerService);

    /**
     * Unbind the given ImporterService of the ImportDeclaration.
     * This method remove the ImporterService to forget the binding.
     * <p/>
     * This method should only be called by a ImportationLinker.
     * The linker must call this method when it remove the ImportDeclaration of the ImporterService.
     *
     * @param importerService the ImporterService the ImportDeclaration is unbind to.
     */
    void unbind(ImporterService importerService);


}
