package org.ow2.chameleon.fuchsia.testing.common;

/*
 * #%L
 * OW2 Chameleon - Fuchsia Base Test: Utilities for test
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

import org.junit.Assert;
import org.junit.Test;
import org.ow2.chameleon.fuchsia.core.component.manager.DeclarationBinder;
import org.ow2.chameleon.fuchsia.core.declaration.Declaration;
import org.ow2.chameleon.fuchsia.core.exceptions.BinderException;
import org.ow2.chameleon.fuchsia.testing.common.platform.GenericImportExporterPlatformTest;

/**
 * Basic Tests that can be done with DeclarationBinder
 * @param <T>
 * @param <S>
 */
public abstract class GenericTest<T extends Declaration,S extends DeclarationBinder> extends GenericImportExporterPlatformTest<T,S> {

    @Test
    public void testValidDeclarations() throws Exception {
        try {
            for(T declaration:getValidDeclarations()){
                fuchsiaDeclarationBinder.useDeclaration(declaration);
            }
        }catch(BinderException be){
            Assert.fail("A BinderException should NOT have been thrown since not all information were provided");
        }
    }

    @Test
    public void testInvalidDeclaration() throws Exception {
        try {
            for(T declaration:getInvalidDeclarations()){
                fuchsiaDeclarationBinder.useDeclaration(declaration);
            }
            Assert.fail("A BinderException should have been thrown since not all information required were provided");
        }catch(BinderException be){
            //An exception for this case is normal, since not all information were provided
        }
    }

}
