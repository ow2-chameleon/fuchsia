package org.ow2.chameleon.fuchsia.discovery.filebased.monitor;

import java.io.File;
import java.util.Collection;

/**
 * Interface which should be implemented by the Component that wishes to monitor state change in a directory
 */
public interface Deployer {

    boolean accept(File file);

    void onFileCreate(File file);

    void onFileChange(File file);

    void onFileDelete(File file);

    void open(Collection<File> files);

    void close();

}
