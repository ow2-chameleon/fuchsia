package org.ow2.chameleon.fuchsia.cxf.config;

import org.apache.felix.ipojo.configuration.Configuration;
import org.apache.felix.ipojo.configuration.Instance;
import org.ow2.chameleon.fuchsia.core.FuchsiaConstants;

import static org.apache.felix.ipojo.configuration.Instance.instance;
import static org.ow2.chameleon.fuchsia.core.ImportationLinker.*;

@Configuration
public class JaxWSInitializer {

    Instance jaxWSInstance = instance()
            .of(FuchsiaConstants.DEFAULT_IMPORTATION_LINKER_FACTORY_NAME)
            .named("DefaultLinkerJaxWS")
            .with(FILTER_IMPORTDECLARATION_PROPERTY).setto("(jax-ws.importer.interfaces=*)")
            .with(FILTER_IMPORTERSERVICE_PROPERTY).setto("(instance.name=Fuchsia_importer_cxf)");

}
