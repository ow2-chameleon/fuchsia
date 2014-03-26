package org.ow2.chameleon.fuchsia.core.component;

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


import org.ow2.chameleon.fuchsia.core.declaration.ExportDeclaration;

/**
 * The components providing this service are used by Fuchsia to make the link between the.
 * {@link ExportDeclaration} and the {@link ExporterService}.
 * You can use multiples {@link ExportationLinker} with different configurations.
 * <p/>
 * A default implementation of {@link ExportationLinker} is provided by the {@link DefaultExportationLinker} component.
 * If the {@link DefaultExportationLinker} doesn't fit to your needs, you can use your own implementation
 * of this interface, by subclassing {@link DefaultExportationLinker} or by implementing this {@link ExportationLinker} interface.
 *
 * @author Morgan Martinet
 */
public interface ExportationLinker {

    String FILTER_EXPORTDECLARATION_PROPERTY = "fuchsia.linker.filter.exportDeclaration";

    String FILTER_EXPORTERSERVICE_PROPERTY = "fuchsia.linker.filter.exporterService";

    String getName();

}
