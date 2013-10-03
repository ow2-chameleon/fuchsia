package org.ow2.chameleon.fuchsia.push.examples.config;

import org.apache.felix.ipojo.configuration.Configuration;
import org.apache.felix.ipojo.configuration.Instance;
import org.ow2.chameleon.fuchsia.core.FuchsiaConstants;

import static org.apache.felix.ipojo.configuration.Instance.instance;
import static org.ow2.chameleon.fuchsia.core.ImportationLinker.FILTER_IMPORTDECLARATION_PROPERTY;
import static org.ow2.chameleon.fuchsia.core.ImportationLinker.FILTER_IMPORTERSERVICE_PROPERTY;

@Configuration
public class PushLinkerInitializer {

    Instance pushLinker = instance()
            .of(FuchsiaConstants.DEFAULT_IMPORTATION_LINKER_FACTORY_NAME)
            .named("PuShHubLinker")
            .with(FILTER_IMPORTDECLARATION_PROPERTY).setto("(push.hub.url=*)")
            .with(FILTER_IMPORTERSERVICE_PROPERTY).setto("(instance.name=PuShHubImporter)");

    Instance pushSubscriber = instance()
            .of("PuShImporterFactory")
            .named("PuShHubImporter")
            .with("target").setto("(push.hub.url=*)");

    Instance pushHub = instance()
            .of("PuSHHubFactory")
            .named("PuShHub")
            .with("target").setto("(dummyPropertyDueToIPojoBug=*)");

}
