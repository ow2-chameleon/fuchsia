package org.ow2.chameleon.fuchsia.push.examples.config;

import org.apache.felix.ipojo.configuration.Configuration;
import org.apache.felix.ipojo.configuration.Instance;
import org.ow2.chameleon.fuchsia.core.FuchsiaConstants;

import static org.apache.felix.ipojo.configuration.Instance.instance;
import static org.ow2.chameleon.fuchsia.core.ImportationLinker.FILTER_IMPORTDECLARATION_PROPERTY;
import static org.ow2.chameleon.fuchsia.core.ImportationLinker.FILTER_IMPORTERSERVICE_PROPERTY;

@Configuration
public class MqttLinkerInitializer {

    Instance pushLinker = instance()
            .of(FuchsiaConstants.DEFAULT_IMPORTATION_LINKER_FACTORY_NAME)
            .named("MQTTLinker")
            .with(FILTER_IMPORTDECLARATION_PROPERTY).setto("(&(id=*)(mqtt.queue=*))")
            .with(FILTER_IMPORTERSERVICE_PROPERTY).setto("(instance.name=AMQPImporter)");

    Instance pushSubscriber = instance()
            .of("AMQPImporterFactory")
            .named("AMQPImporter")
            .with("target").setto("(&(id=*)(mqtt.queue=*))");

}
