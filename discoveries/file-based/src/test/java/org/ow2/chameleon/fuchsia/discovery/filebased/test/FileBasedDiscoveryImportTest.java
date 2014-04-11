/*
 * #%L
 * OW2 Chameleon - Fuchsia Framework
 * %%
 * Copyright (C) 2009 - 2014 OW2 Chameleon
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
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