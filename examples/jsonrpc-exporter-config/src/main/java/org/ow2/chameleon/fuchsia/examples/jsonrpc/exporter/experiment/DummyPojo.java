package org.ow2.chameleon.fuchsia.examples.jsonrpc.exporter.experiment;

/*
 * #%L
 * OW2 Chameleon - Fuchsia Example JSONRPC Base interface
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

/**
 * Created with IntelliJ IDEA.
 * User: jnascimento
 * Date: 27/01/14
 * Time: 10:17
 * To change this template use File | Settings | File Templates.
 */
@Component
@Instantiate(name = "DummyPojoInstance")
@Provides
public class DummyPojo implements DummyIface {

    public void helloworld(String value){

        System.out.println("hello "+value);

    }

}
