package org.ow2.chameleon.fuchsia.raspberry.pi.testing;

import org.apache.felix.ipojo.configuration.Configuration;
import org.apache.felix.ipojo.configuration.Instance;
import org.ow2.chameleon.fuchsia.core.FuchsiaConstants;
import org.ow2.chameleon.fuchsia.core.ImportationLinker;
import org.ow2.chameleon.fuchsia.discovery.filebased.FileBasedDiscoveryImportBridge;

import static org.apache.felix.ipojo.configuration.Instance.instance;

@Configuration
public class Config {


    Instance gpioFilebased = instance()
            .of(FileBasedDiscoveryImportBridge.class.getName());

    Instance gpioImporter = instance()
            .of("GpIOImporter")
            .named("gpioImporter")
            .with("target").setto("(id=*)");

    Instance gpioLinker = instance()
            .of(FuchsiaConstants.DEFAULT_IMPORTATION_LINKER_FACTORY_NAME)
            .named("gpioLinker")
            .with(ImportationLinker.FILTER_IMPORTDECLARATION_PROPERTY).setto("(id=*)")
            .with(ImportationLinker.FILTER_IMPORTERSERVICE_PROPERTY).setto("(instance.name=gpioImporter)");

    /*
    Instance gpioLinker = instance()
            .of(FuchsiaConstants.DEFAULT_IMPORTATION_LINKER_FACTORY_NAME)
            .with(ImportationLinker.FILTER_IMPORTDECLARATION_PROPERTY).setto("(&(importer.gpio.pin=*)(importer.gpio.name=*)(id=*))")
            .with(ImportationLinker.FILTER_IMPORTERSERVICE_PROPERTY).setto("(instance.name=gpioImporter)");
    */


}
