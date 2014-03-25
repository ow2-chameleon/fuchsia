package org.ow2.chameleon.fuchsia.discovery.filebased.monitor;

/*
 * #%L
 * OW2 Chameleon - Fuchsia Discovery FileBased
 * %%
 * Copyright (C) 2009 - 2014 OW2 Chameleon
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;


public class DirectoryMonitor implements BundleActivator, ServiceTrackerCustomizer {

    /**
     * logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(DirectoryMonitor.class);

    /**
     * List of deployers
     */
    private final List<Deployer> deployers = new ArrayList<Deployer>();
    /**
     * The directory.
     */
    private final File directory;
    /**
     * Polling period.
     * -1 to disable polling.
     */
    private final long polling;
    /**
     * A monitor listening file changes.
     */
    private final FileAlterationMonitor monitor;
    /**
     * The lock avoiding concurrent modifications of the deployers map.
     */
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    private final String trackedClassName;
    /**
     * Service tracking to retrieve deployers.
     */
    private ServiceTracker tracker;
    private BundleContext context;


    public DirectoryMonitor(String directorypath, long polling, String classname) {

        this.directory = new File(directorypath);
        this.trackedClassName = classname;
        this.polling = polling;

        if (!directory.isDirectory()) {
            LOG.info("Monitored directory {} not existing - creating directory", directory.getAbsolutePath());
            if (!this.directory.mkdirs()) {
                throw new IllegalStateException("Monitored directory doesn't exist and cannot be created.");
            }
        }

        // We observes all files.
        FileAlterationObserver observer = new FileAlterationObserver(directory, TrueFileFilter.INSTANCE);
        observer.checkAndNotify();
        observer.addListener(new FileMonitor());
        monitor = new FileAlterationMonitor(polling, observer);

    }

    /**
     * Acquires the write lock only and only if the write lock is not already held by the current thread.
     *
     * @return {@literal true} if the lock was acquired within the method, {@literal false} otherwise.
     */
    private boolean acquireWriteLockIfNotHeld() {
        if (!lock.isWriteLockedByCurrentThread()) {
            lock.writeLock().lock();
            return true;
        }
        return false;
    }

    /**
     * Releases the write lock only and only if the write lock is held by the current thread.
     *
     * @return {@literal true} if the lock has no more holders, {@literal false} otherwise.
     */
    private boolean releaseWriteLockIfHeld() {
        if (lock.isWriteLockedByCurrentThread()) {
            lock.writeLock().unlock();
        }
        return lock.getWriteHoldCount() == 0;
    }

    /**
     * Acquires the read lock only and only if no read lock is already held by the current thread.
     *
     * @return {@literal true} if the lock was acquired within the method, {@literal false} otherwise.
     */
    private boolean acquireReadLockIfNotHeld() {
        if (lock.getReadHoldCount() == 0) {
            lock.readLock().lock();
            return true;
        }
        return false;
    }

    /**
     * Releases the read lock only and only if the read lock is held by the current thread.
     *
     * @return {@literal true} if the lock has no more holders, {@literal false} otherwise.
     */
    private boolean releaseReadLockIfHeld() {
        if (lock.getReadHoldCount() != 0) {
            lock.readLock().unlock();
        }
        return lock.getReadHoldCount() == 0;
    }

    public void start(final BundleContext context) throws DirectoryMonitoringException {
        this.context = context;
        LOG.info("Starting installing bundles from {}", directory.getAbsolutePath());
        this.tracker = new ServiceTracker(context, this.trackedClassName, this);

        // To avoid concurrency, we take the write lock here.
        try {
            acquireWriteLockIfNotHeld();

            // Arrives will be blocked until we release teh write lock
            this.tracker.open();

            // Register file monitor
            startFileMonitoring();

        } finally {
            releaseWriteLockIfHeld();
        }

        // Initialization does not need the write lock, read is enough.
        try {
            acquireReadLockIfNotHeld();
            // Per extension, open deployer.
            Collection<File> files = FileUtils.listFiles(directory, null, true);
            for (File file : files) {
                for (Deployer deployer : deployers) {
                    if (deployer.accept(file)) {
                        deployer.open(files);
                    }
                }
            }
        } finally {
            releaseReadLockIfHeld();
        }
    }

    private void startFileMonitoring() throws DirectoryMonitoringException {
        if (polling == -1L) {
            LOG.debug("No file monitoring for {}", directory.getAbsolutePath());
            return;
        }

        LOG.info("Starting file monitoring for {} - polling : {} ms", directory.getName(), polling);
        try {
            monitor.start();
        } catch (Exception e) {
            throw new DirectoryMonitoringException("Exception while starting the FileAlterationMonitor.", e);
        }
    }

    public void stop(BundleContext context) throws DirectoryMonitoringException {
        // To avoid concurrency, we take the write lock here.
        try {
            acquireWriteLockIfNotHeld();
            this.tracker.close();
            if (monitor != null) {
                LOG.debug("Stopping file monitoring of {}", directory.getAbsolutePath());
                // Wait 5 milliseconds.
                monitor.stop(5);
            }
        } catch (Exception e) {
            throw new DirectoryMonitoringException("Exception while stopping the FileAlterationMonitor.", e);
        } finally {
            releaseWriteLockIfHeld();
        }

        // No concurrency involved from here.
        for (Deployer deployer : deployers) {
            deployer.close();
        }
    }

    public Object addingService(ServiceReference reference) {

        Deployer deployer = (Deployer) context.getService(reference);

        try {
            acquireWriteLockIfNotHeld();
            deployers.add(deployer);
            Collection<File> files = FileUtils.listFiles(directory, null, true);
            List<File> accepted = new ArrayList<File>();
            for (File file : files) {
                if (deployer.accept(file)) {
                    accepted.add(file);
                }
            }
            deployer.open(accepted);
        } finally {
            releaseWriteLockIfHeld();
        }

        return deployer;

    }

    public void modifiedService(ServiceReference reference, Object o) {
        // Cannot happen, deployers do not have properties.
    }

    public void removedService(ServiceReference reference, Object o) {
        Deployer deployer = (Deployer) o;
        try {
            acquireWriteLockIfNotHeld();
            deployers.remove(deployer);
        } finally {
            releaseWriteLockIfHeld();
        }
    }

    private class FileMonitor extends FileAlterationListenerAdaptor {

        /**
         * A jar file was created.
         *
         * @param file the file
         */
        @Override
        public void onFileCreate(File file) {
            LOG.info("File " + file + " created in " + directory);

            // Callback called outside the protected region.
            for (Deployer deployer : getDeployers(file)) {
                try {
                    deployer.onFileCreate(file);
                } catch (Exception e) {
                    LOG.error("Error during the management of " + file.getAbsolutePath() + " (created) by " + deployer, e);
                }
            }
        }

        @Override
        public void onFileChange(File file) {
            LOG.info("File " + file + " from " + directory + " changed");

            for (Deployer deployer : getDeployers(file)) {
                try {
                    deployer.onFileChange(file);
                } catch (Exception e) {
                    LOG.error("Error during the management of " + file.getAbsolutePath() + " (change) by " + deployer,
                            e);
                }
            }
        }

        @Override
        public void onFileDelete(File file) {
            LOG.info("File " + file + " deleted from " + directory);

            for (Deployer deployer : getDeployers(file)) {
                try {
                    deployer.onFileDelete(file);
                } catch (Exception e) {
                    LOG.error("Error during the management of " + file.getAbsolutePath() + " (delete) by " + deployer, e);
                }
            }
        }
    }

    private Set<Deployer> getDeployers(File file) {
        Set<Deployer> depl = new HashSet<Deployer>();
        try {
            acquireReadLockIfNotHeld();
            for (Deployer deployer : deployers) {
                if (deployer.accept(file)) {
                    depl.add(deployer);
                }
            }
        } finally {
            releaseReadLockIfHeld();
        }
        return depl;
    }
}
