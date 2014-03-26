package org.ow2.chameleon.fuchsia.discovery.filebased.test;

import org.osgi.framework.BundleContext;
import org.ow2.chameleon.fuchsia.discovery.filebased.FileBasedDiscoveryExport;
import org.ow2.chameleon.fuchsia.discovery.filebased.test.util.FilebasedTestAbstract;

import static org.fest.reflect.core.Reflection.constructor;
import static org.fest.reflect.core.Reflection.field;
import static org.mockito.Mockito.spy;

public class FileBasedDiscoveryExportTest extends FilebasedTestAbstract<FileBasedDiscoveryExport> {

    @Override
    public void init() {
        discovery =spy(constructor().withParameterTypes(BundleContext.class).in(FileBasedDiscoveryExport.class).newInstance(context));
        field("monitoredExportDirectory").ofType(String.class).in(discovery).set(tempFolder.getRoot().getAbsolutePath());
    }

}