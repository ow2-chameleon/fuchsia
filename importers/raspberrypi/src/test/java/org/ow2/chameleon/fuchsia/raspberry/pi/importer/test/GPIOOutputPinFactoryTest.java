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
package org.ow2.chameleon.fuchsia.raspberry.pi.importer.test;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.ow2.chameleon.fuchsia.raspberry.pi.controller.PiController;
import org.ow2.chameleon.fuchsia.raspberry.pi.device.GPIOOutputPinFactory;

import static org.fest.reflect.core.Reflection.constructor;
import static org.fest.reflect.core.Reflection.field;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class GPIOOutputPinFactoryTest {

    GPIOOutputPinFactory factory;

    @Mock
    PiController controller;

    @Before
    public void init(){
        MockitoAnnotations.initMocks(this);
        factory=spy(constructor().in(GPIOOutputPinFactory.class).newInstance());
        field("controller").ofType(PiController.class).in(factory).set(controller);

    }

    @Test
    public void TestInvokeOn(){
        factory.invoke("on");
        verify(controller,times(1)).writePin(anyInt(),eq(1));
    }

    @Test
    public void TestInvokeOff(){
        factory.invoke("off");
        verify(controller,times(1)).writePin(anyInt(),eq(0));
    }

    @Test
    public void TestInvokeTransaction(){
        factory.invoke("on",1,null);
        verify(factory,times(1)).invoke(anyString());
    }

}
