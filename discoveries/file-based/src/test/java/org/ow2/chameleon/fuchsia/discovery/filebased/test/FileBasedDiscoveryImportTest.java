package org.ow2.chameleon.fuchsia.discovery.filebased.test;

import org.osgi.framework.BundleContext;
import org.ow2.chameleon.fuchsia.discovery.filebased.FileBasedDiscoveryImport;
import org.ow2.chameleon.fuchsia.discovery.filebased.test.util.FilebasedTestAbstract;

import static org.fest.reflect.core.Reflection.constructor;
import static org.fest.reflect.core.Reflection.field;
import static org.mockito.Mockito.spy;

public class FileBasedDiscoveryImportTest extends FilebasedTestAbstract<FileBasedDiscoveryImport> {

    @Override
    public void init() {
        discovery =spy(constructor().withParameterTypes(BundleContext.class).in(FileBasedDiscoveryImport.class).newInstance(context));
        field("monitoredImportDirectory").ofType(String.class).in(discovery).set(tempFolder.getRoot().getAbsolutePath());
    }


}