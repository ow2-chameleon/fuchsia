package org.ow2.chameleon.fuchsia.fool;

import org.apache.felix.ipojo.configuration.Configuration;
import org.apache.felix.ipojo.configuration.Instance;

import static org.apache.felix.ipojo.Factory.INSTANCE_NAME_PROPERTY;
import static org.apache.felix.ipojo.configuration.Instance.instance;
import static org.ow2.chameleon.fuchsia.core.FuchsiaConstants.DEFAULT_IMPORTATION_LINKER_FACTORY_NAME;
import static org.ow2.chameleon.fuchsia.core.ImportationLinker.FILTER_IMPORTDECLARATION_PROPERTY;
import static org.ow2.chameleon.fuchsia.core.ImportationLinker.FILTER_IMPORTERSERVICE_PROPERTY;

@Configuration
public class FoolInitializer {

    Instance roseMachineInstance = instance()
            .of(DEFAULT_IMPORTATION_LINKER_FACTORY_NAME)
            .named("FoolLinker")
            .with(FILTER_IMPORTDECLARATION_PROPERTY).setto("(fool=fool)")
            .with(FILTER_IMPORTERSERVICE_PROPERTY).setto("(" + INSTANCE_NAME_PROPERTY + "=Fuchsia-FoolImporter)");

}
