package org.ow2.chameleon.fuchsia.fool;

import org.apache.felix.ipojo.configuration.Configuration;
import org.apache.felix.ipojo.configuration.Instance;

import static org.apache.felix.ipojo.configuration.Instance.instance;
import static org.ow2.chameleon.fuchsia.core.ImportationLinker.PROPERTY_FILTER_IMPORTDECLARATION;
import static org.ow2.chameleon.fuchsia.core.ImportationLinker.PROPERTY_FILTER_IMPORTERSERVICE;

@Configuration
public class FoolInitializer {

    Instance roseMachineInstance = instance()
            .of("FuchsiaDefaultLinkerFactory")
            .named("FoolLinker")
            .with(PROPERTY_FILTER_IMPORTDECLARATION).setto("(fool=fool)")
            .with(PROPERTY_FILTER_IMPORTERSERVICE).setto("(instance.name=Fuchsia-FoolImporter)");

}
