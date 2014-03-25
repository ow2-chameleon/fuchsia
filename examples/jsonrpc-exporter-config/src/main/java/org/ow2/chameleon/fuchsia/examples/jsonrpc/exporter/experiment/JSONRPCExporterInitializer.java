package org.ow2.chameleon.fuchsia.examples.jsonrpc.exporter.experiment;

/*
 * #%L
 * OW2 Chameleon - Fuchsia Example JSONRPC Base interface
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

import org.apache.felix.ipojo.configuration.Configuration;
import org.apache.felix.ipojo.configuration.Instance;
import org.ow2.chameleon.fuchsia.core.component.ExportationLinker;
import org.ow2.chameleon.fuchsia.core.FuchsiaConstants;
import org.ow2.chameleon.fuchsia.core.component.manager.DeclarationBinder;

import static org.ow2.chameleon.fuchsia.core.component.ImportationLinker.FILTER_IMPORTDECLARATION_PROPERTY;
import static org.ow2.chameleon.fuchsia.core.component.ImportationLinker.FILTER_IMPORTERSERVICE_PROPERTY;

import static org.apache.felix.ipojo.configuration.Instance.instance;

@Configuration
public class JSONRPCExporterInitializer {

        Instance jsonRPCExporter = instance()
                .of("org.ow2.chameleon.fuchsia.exporter.jsonrpc.JSONRPCExporter")
                .with("target").setto("(fuchsia.export.jsonrpc.instance=*)");

        Instance jsonRPCExporterLinker = instance()
            .of(FuchsiaConstants.DEFAULT_EXPORTATION_LINKER_FACTORY_NAME)
            .with(ExportationLinker.FILTER_EXPORTDECLARATION_PROPERTY).setto("(fuchsia.export.jsonrpc.instance=*)")
            .with(ExportationLinker.FILTER_EXPORTERSERVICE_PROPERTY).setto("(instance.name=jsonRPCExporter)");


        Instance jsonRPCImporter = instance()
            .of("Fuchsia-Importer:JSON-RPC")
            .with(DeclarationBinder.TARGET_FILTER_PROPERTY).setto("(configs=jsonrpc)");

        Instance jsonRPCImporterLinker = instance()
            .of(FuchsiaConstants.DEFAULT_IMPORTATION_LINKER_FACTORY_NAME)
            .with(FILTER_IMPORTDECLARATION_PROPERTY).setto("(configs=jsonrpc)")
            .with(FILTER_IMPORTERSERVICE_PROPERTY).setto("(instance.name=jsonRPCImporter)");



}
