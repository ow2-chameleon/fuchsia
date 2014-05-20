package org.ow2.chameleon.fuchsia.importer.jaxws.test.ctd;

/*
 * #%L
 * OW2 Chameleon - Fuchsia Importer JAX-WS
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

import org.osgi.framework.Bundle;

/**
 * CrashTestDummie class for the test.
 */
public class ServiceForExportationimpl implements ServiceForExportation {

    public void ping() {
        System.out.println("ping received");
    }

    public void ping(String value) {
        System.out.println("ping string " + value + " received");
    }

    public void ping(Integer value) {
        System.out.println("ping int " + value + " received");
    }

    public String pongString(String input) {
        return input;
    }

    public Integer pongInteger(Integer input) {
        return input;
    }

    public Object getProperty(String key) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String[] getPropertyKeys() {
        return new String[0];  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Bundle getBundle() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Bundle[] getUsingBundles() {
        return new Bundle[0];  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean isAssignableTo(Bundle bundle, String className) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public int compareTo(Object reference) {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

}
