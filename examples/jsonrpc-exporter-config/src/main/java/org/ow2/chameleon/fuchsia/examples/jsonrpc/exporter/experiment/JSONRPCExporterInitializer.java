package org.ow2.chameleon.fuchsia.examples.jsonrpc.exporter.experiment;

import org.apache.felix.ipojo.configuration.Configuration;
import org.apache.felix.ipojo.configuration.Instance;
import org.ow2.chameleon.fuchsia.core.ExportationLinker;
import org.ow2.chameleon.fuchsia.core.FuchsiaConstants;
import org.ow2.chameleon.fuchsia.core.component.ImporterService;

import static org.ow2.chameleon.fuchsia.core.ImportationLinker.FILTER_IMPORTDECLARATION_PROPERTY;
import static org.ow2.chameleon.fuchsia.core.ImportationLinker.FILTER_IMPORTERSERVICE_PROPERTY;

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
            .with(ImporterService.TARGET_FILTER_PROPERTY).setto("(configs=jsonrpc)");

        Instance jsonRPCImporterLinker = instance()
            .of(FuchsiaConstants.DEFAULT_IMPORTATION_LINKER_FACTORY_NAME)
            .with(FILTER_IMPORTDECLARATION_PROPERTY).setto("(configs=jsonrpc)")
            .with(FILTER_IMPORTERSERVICE_PROPERTY).setto("(instance.name=jsonRPCImporter)");



}
