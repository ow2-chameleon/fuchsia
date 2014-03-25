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

import java.util.Set;

/**
 * Created by mo on 19/02/14.
 */
public interface ExportationLinkerIntrospection {
    /**
     * Return the exporterServices linked the ExportationLinker
     *
     * @return The exporterServices linked to the ExportationLinker
     */
    Set<ExporterService> getLinkedExporters();

    /**
     * Return the exportDeclarations bind by the ExportationLinker
     *
     * @return The exportDeclarations bind by the ExportationLinker
     */
    Set<ExportDeclaration> getExportDeclarations();
}
