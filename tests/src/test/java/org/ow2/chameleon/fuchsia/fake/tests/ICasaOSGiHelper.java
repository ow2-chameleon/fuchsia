/**
 *
 *   Copyright 2011-2012 Universite Joseph Fourier, LIG, ADELE Research
 *   Group Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package org.ow2.chameleon.fuchsia.fake.tests;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.ow2.chameleon.testing.helpers.OSGiHelper;

import static org.junit.Assert.fail;

public class ICasaOSGiHelper extends OSGiHelper {

    /**
     * The bundle context.
     */
    private BundleContext _context;

    public ICasaOSGiHelper(BundleContext context) {
        super(context);
        _context = context;
    }

    /**
     * Waits for a service. Fails on timeout.
     * If timeout is set to 0, it sets the timeout to 10s.
     * @param registeredItf the service interface
     * @param filter the filter
     * @param timeout the timeout
     */
    public void waitForService(String registeredItf, String filter, long timeout, Class... implementedItfs) {
        if (timeout == 0) {
            timeout = 10000; // Default 10 secondes.
        }
        ServiceReference[] refs = getServiceReferences(registeredItf, filter);
        long begin = System.currentTimeMillis();
        while (true) {
            refs = getServiceReferences(registeredItf, filter);
            if (refs != null) {
                for (ServiceReference sref : refs) {
                    Object serv = getServiceObject(sref);
                    boolean implOk = true;
                    for (Class implCl : implementedItfs) {
                        if (!(implCl.isAssignableFrom(serv.getClass()))) {
                            implOk = false;
                            break;
                        }
                    }
                    if (implOk)
                        return;
                }
            }

            try {
                Thread.sleep(5);
            } catch (InterruptedException e) {
// Interrupted
            }
            long now = System.currentTimeMillis();

            if ((now - begin) > timeout) {
                fail("Timeout ... no services matching with the request after "
                        + timeout + "ms");
            }
        }
    }

    /**
     * Checks that a service is unavailable for specified time period.
     *
     *
     * If timeout is set to 0, it sets the timeout to 2s.
     * @param registeredItf the service interface
     * @param filter the filter
     * @param checkPeriod the time period
     */
    public void checkUnavailableService(String registeredItf, String filter, long checkPeriod, Class... implementedItfs) {
        if (checkPeriod == 0) {
            checkPeriod = 2000; // Default 10 secondes.
        }

        ServiceReference[] refs = getServiceReferences(registeredItf, filter);
        long begin = System.currentTimeMillis();
        while (true) {
            refs = getServiceReferences(registeredItf, filter);
            if (refs != null) {
                for (ServiceReference sref : refs) {
                    Object serv = getServiceObject(sref);
                    boolean implOk = true;
                    for (Class implCl : implementedItfs) {
                        if (!(implCl.isAssignableFrom(serv.getClass()))) {
                            implOk = false;
                            break;
                        }
                    }
                    if (implOk)
                        fail("A services matching with the request has been found");
                }
            }

            try {
                Thread.sleep(5);
            } catch (InterruptedException e) {
// Interrupted
            }
            long now = System.currentTimeMillis();

            if ((now - begin) > checkPeriod) {
                return;
            }
        }
    }
}