package org.ow2.chameleon.fuchsia.philips.hue.examples;

import org.apache.felix.ipojo.configuration.Configuration;
import org.apache.felix.ipojo.configuration.Instance;
import org.ow2.chameleon.fuchsia.core.FuchsiaConstants;
import org.ow2.chameleon.fuchsia.core.component.ImportationLinker;

import static org.apache.felix.ipojo.configuration.Instance.instance;

@Configuration
public class PhilipsHueImporterConfig {

    Instance philipsImporter = instance()
            .of("org.ow2.chameleon.fuchsia.importer.philipshue.PhilipsHueImporter")
            .with("target").setto("(discovery.philips.device.name=*)");

    Instance philipsLinker = instance()
            .of(FuchsiaConstants.DEFAULT_IMPORTATION_LINKER_FACTORY_NAME)
            .with(ImportationLinker.FILTER_IMPORTDECLARATION_PROPERTY).setto("(discovery.philips.device.name=*)")
            .with(ImportationLinker.FILTER_IMPORTERSERVICE_PROPERTY).setto("(instance.name=philipsImporter)");

}
