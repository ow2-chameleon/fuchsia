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


/**
 * The components providing this service are used by Fuchsia to make the link between the.
 * {@link org.ow2.chameleon.fuchsia.core.declaration.ImportDeclaration} and the {@link org.ow2.chameleon.fuchsia.core.component.ImporterService}.
 * You can use multiples {@link ImportationLinker} with different configurations.
 * <p/>
 * A default implementation of {@link ImportationLinker} is provided by the {@link DefaultImportationLinker} component.
 * If the {@link DefaultImportationLinker} doesn't fit to your needs, you can use your own implementation
 * of this interface, by subclassing {@link DefaultImportationLinker} or by implementing this {@link ImportationLinker} interface.
 *
 * @author Morgan Martinet
 */
public interface ImportationLinker {

    String FILTER_IMPORTDECLARATION_PROPERTY = "fuchsia.linker.filter.importDeclaration";

    String FILTER_IMPORTERSERVICE_PROPERTY = "fuchsia.linker.filter.importerService";

    String getName();

}
