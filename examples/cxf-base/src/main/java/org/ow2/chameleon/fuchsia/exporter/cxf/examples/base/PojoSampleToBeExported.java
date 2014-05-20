package org.ow2.chameleon.fuchsia.exporter.cxf.examples.base;

/*
 * #%L
 * OW2 Chameleon - Fuchsia Example CXF Base interface
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


import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;

@Component
@Instantiate(name = "PojoSampleToBeExported")
@Provides
public class PojoSampleToBeExported implements PojoSampleToBeExportedIface {

    public void showMessage2() {
        System.out.println("ok");
    }

    public String getMessage2() {
        return "ok";
    }

}
