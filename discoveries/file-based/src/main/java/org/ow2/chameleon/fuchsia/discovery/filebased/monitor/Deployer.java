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
