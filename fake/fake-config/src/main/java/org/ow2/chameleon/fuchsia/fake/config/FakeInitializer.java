package org.ow2.chameleon.fuchsia.fake.config;

import org.apache.felix.ipojo.configuration.Configuration;
import org.apache.felix.ipojo.configuration.Instance;
import org.ow2.chameleon.fuchsia.core.FuchsiaConstants;

import static org.apache.felix.ipojo.configuration.Instance.instance;
import static org.ow2.chameleon.fuchsia.core.ImportationLinker.*;

@Configuration
public class FakeInitializer {

    Instance roseMachineInstance = instance()
            .of(FuchsiaConstants.DEFAULT_IMPORTATION_LINKER_FACTORY_NAME)
            .named("DefaultLinkerFake")
            .with(FILTER_IMPORTDECLARATION_PROPERTY).setto("(id=*)")
            .with(FILTER_IMPORTERSERVICE_PROPERTY).setto("(instance.name=Fuchsia-FakeImporter)");

}
