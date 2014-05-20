package org.ow2.chameleon.fuchsia.core.it.util;

/*
 * #%L
 * OW2 Chameleon - Fuchsia Core [IntegrationTests]
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

import org.junit.Test;
import org.ow2.chameleon.fuchsia.core.FuchsiaUtils;
import org.ow2.chameleon.fuchsia.core.component.DefaultImportationLinker;
import org.ow2.chameleon.fuchsia.testing.Common;

import static org.assertj.core.api.Assertions.assertThat;

public class FuchsiaUtilsTest extends Common {

    @Override
    public boolean deployTestBundle() {
        return false;
    }

    @Test
    public void testLoadClass() throws ClassNotFoundException {
        Class<?> aClass = FuchsiaUtils.loadClass(this.getContext(), DefaultImportationLinker.class.getName());
        assertThat(aClass).isNotNull().isSameAs(DefaultImportationLinker.class);
    }


    @Test
    public void testLoadClassFail() throws ClassNotFoundException {
        String klassName = "i.must.does.not.exist.MyClass";

        try {
            Class<?> aClass = FuchsiaUtils.loadClass(this.getContext(), klassName);
        } catch (ClassNotFoundException e) {
            assertThat(e).hasMessageContaining(klassName);
        }
    }

    @Test
    public void testLoadClassNew() throws ClassNotFoundException {
        Class<?> aClass = FuchsiaUtils.loadClassNew(this.getContext(), DefaultImportationLinker.class.getName());
        assertThat(aClass).isNotNull().isSameAs(DefaultImportationLinker.class);
    }


    @Test
    public void testLoadClassNewFail() throws ClassNotFoundException {
        String klassName = "i.must.does.not.exist.MyClass";

        try {
            Class<?> aClass = FuchsiaUtils.loadClassNew(this.getContext(), klassName);
        } catch (ClassNotFoundException e) {
            assertThat(e).hasMessageContaining(klassName);
        }
    }
}
