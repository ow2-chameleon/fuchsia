package org.ow2.chameleon.fuchsia.fake.discovery.monitor;

import java.io.File;
import java.util.Collection;

/**
 * Interface which should be implemented by the Component that wishes to monitor state change in a directory
 */
public interface Deployer {

    public boolean accept(File file);

    public void onFileCreate(File file);

    public void onFileChange(File file);

    public void onFileDelete(File file);

    public void open(Collection<File> files);

    public void close();

}
