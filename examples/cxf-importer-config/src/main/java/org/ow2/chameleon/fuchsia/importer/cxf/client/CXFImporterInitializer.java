package org.ow2.chameleon.fuchsia.importer.cxf.client;

import org.apache.felix.ipojo.configuration.Configuration;
import org.apache.felix.ipojo.configuration.Instance;
import org.ow2.chameleon.fuchsia.core.FuchsiaConstants;

import static org.apache.felix.ipojo.configuration.Instance.instance;
import static org.ow2.chameleon.fuchsia.core.component.ImportationLinker.FILTER_IMPORTDECLARATION_PROPERTY;
import static org.ow2.chameleon.fuchsia.core.component.ImportationLinker.FILTER_IMPORTERSERVICE_PROPERTY;

@Configuration
public class CXFImporterInitializer {

    Instance cxfimporter = instance()
            .of("org.ow2.chameleon.fuchsia.importer.jaxws.JAXWSImporter")
            .named("cxfimporter")
            .with("target").setto("(endpoint.url=*)");

    Instance cxfimporterlinker = instance()
            .of(FuchsiaConstants.DEFAULT_IMPORTATION_LINKER_FACTORY_NAME)
            .named("cxfimporterlinker")
            .with(FILTER_IMPORTDECLARATION_PROPERTY).setto("(endpoint.url=*)")
            .with(FILTER_IMPORTERSERVICE_PROPERTY).setto("(instance.name=cxfimporter)");

}
