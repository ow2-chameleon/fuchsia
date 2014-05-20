package org.ow2.chameleon.fuchsia.discovery.filebased;

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

/**
 * The class with the constants used in the file based discovery.
 */
public class FileBasedDiscoveryConstants {

    public static final String FILEBASED_DISCOVERY_MONITORED_DIR_KEY = "fuchsia.system.filebased.discovery.directory";

    public static final String FILEBASED_DISCOVERY_IMPORT_PROPERTY_KEY_MONITORED_DIR_VALUE = "load/import";
    public static final String FILEBASED_DISCOVERY_EXPORT_PROPERTY_KEY_MONITORED_DIR_VALUE = "load/export";

    public static final String FILEBASED_DISCOVERY_PROPERTY_POLLING_TIME_KEY = "fuchsia.system.filebased.discovery.polling";
    public static final String FILEBASED_DISCOVERY_PROPERTY_POLLING_TIME_VALUE = "2000";

    private FileBasedDiscoveryConstants() {

    }
}
