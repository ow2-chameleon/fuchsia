package org.ow2.chameleon.fuchsia.core.declaration;

/*
 * #%L
 * OW2 Chameleon - Fuchsia Core
 * %%
 * Copyright (C) 2009 - 2014 OW2 Chameleon
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import org.osgi.framework.ServiceReference;
import org.ow2.chameleon.fuchsia.core.component.DiscoveryService;

import java.util.Map;

/**
 * {@link Declaration} is a data transfer object (DTO pattern) that transit between layers
 * in Fuchsia.
 * They are created by the {@link DiscoveryService}
 *
 * @author Morgan Martinet
 */
public interface Declaration {

    /**
     * @return the metadata of the Declaration
     */
    Map<String, Object> getMetadata();

    /**
     * @return the extra-metadata of the Declaration
     */
    Map<String, Object> getExtraMetadata();

    /**
     * Return the Status of the Declaration at the moment t of the method call.
     * The status contains the bindings which exist between the Declaration and
     * the ImporterServices.
     *
     * @return the actual (at this moment t) Declaration status.
     */
    Status getStatus();

    /**
     * Bind the given ImporterService to the Declaration.
     * This method stock the ImporterService to remember the binding.
     * <p/>
     * This method should only be called by a ImportationLinker.
     * The linker must call this method when it give the Declaration to the ImporterService.
     *
     * @param serviceReference the ServiceReference of the ImporterService the Declaration is bind to.
     */
    void bind(ServiceReference serviceReference);

    /**
     * Unbind the given ImporterService of the Declaration.
     * This method remove the ImporterService to forget the binding.
     * <p/>
     * This method should only be called by a ImportationLinker.
     * The linker must call this method when it remove the Declaration of the ImporterService.
     *
     * @param serviceReference the ServiceReference of the ImporterService the Declaration is unbind to.
     */
    void unbind(ServiceReference serviceReference);


    /**
     * Set the Declaration as handled by the given ImporterService.
     * This method stock the ImporterService to remember the handling.
     * <p/>
     * This method should only be called by the ImporterService which handle the Declaration.
     * The ImporterService must call this method when it uses with the Declaration.
     *
     * @param serviceReference the ServiceReference of the ImporterService that handle the Declaration.
     */
    void handle(ServiceReference serviceReference);

    /**
     * Set the Declaration as unhandled by the given ImporterService.
     * This method remove the ImporterService to forget the handling.
     * <p/>
     * This method should only be called by the ImporterService which unhandle the Declaration.
     * The ImporterService must call this method when it stops to use the Declaration.
     *
     * @param serviceReference the ServiceReference of the ImporterService that unhandle the Declaration.
     */
    void unhandle(ServiceReference serviceReference);

}
