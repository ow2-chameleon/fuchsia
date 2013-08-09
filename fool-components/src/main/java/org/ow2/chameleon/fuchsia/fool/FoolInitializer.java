package org.ow2.chameleon.fuchsia.fool;

import org.apache.felix.ipojo.configuration.Configuration;
import org.apache.felix.ipojo.configuration.Instance;

import static org.apache.felix.ipojo.configuration.Instance.instance;
import static org.ow2.chameleon.fuchsia.core.Linker.PROPERTY_FILTER_IMPORTDECLARATION;

@Configuration
public class FoolInitializer {

    Instance roseMachineInstance = instance()
            .of("FuchsiaDefaultLinkerFactory")
            .named("FoolLinker")
            .with(PROPERTY_FILTER_IMPORTDECLARATION).setto("(fool=fool)");

}
