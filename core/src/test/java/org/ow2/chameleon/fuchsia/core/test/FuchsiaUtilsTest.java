package org.ow2.chameleon.fuchsia.core.test;

/*
 * #%L
 * OW2 Chameleon - Fuchsia Core
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
import org.osgi.framework.Filter;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.ow2.chameleon.fuchsia.core.FuchsiaUtils;
import org.ow2.chameleon.fuchsia.core.exceptions.InvalidFilterException;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

public class FuchsiaUtilsTest {

    @Test
    public void testGetFilterFromString() {
        Filter f = null;
        try {
            f = FuchsiaUtils.getFilter("(!(key=value))");
        } catch (InvalidFilterException e) {
            fail("GetFilter thrown an exception on a valid String filter", e);
        }
        assertThat(f).isNotNull().isInstanceOf(Filter.class);

        Map<String, Object> map = new HashMap<String, Object>();
        map.put("key", "value");
        assertThat(f.matches(map)).isFalse();

        map.clear();
        map.put("key", "not-value");
        assertThat(f.matches(map)).isTrue();

        map.clear();
        map.put("not-key", "value");
        assertThat(f.matches(map)).isTrue();
    }

    @Test
    public void testGetFilterFromInvalidString() {
        try {
            FuchsiaUtils.getFilter("(!(key=value)");
            failBecauseExceptionWasNotThrown(InvalidFilterException.class);
        } catch (Exception e) {
            assertThat(e).isInstanceOf(InvalidFilterException.class)
                    .hasCauseInstanceOf(InvalidSyntaxException.class)
                    .hasMessage("The String given has filter doesn't respect the LDAP syntax");
        }
    }


    @Test
    public void testGetFilterFromFilter() {
        Filter myFilter = null;
        Filter f = null;

        try {
            myFilter = FrameworkUtil.createFilter("(!(key=value))");
        } catch (InvalidSyntaxException e) {
            fail("Can't create a filter with org.apache.felix.framework.FilterImpl", e);
        }
        try {
            f = FuchsiaUtils.getFilter(myFilter);
        } catch (InvalidFilterException e) {
            fail("GetFilter thrown an exception on a valid String filter", e);
        }
        assertThat(f).isNotNull().isInstanceOf(Filter.class);

        Map<String, Object> map = new HashMap<String, Object>();
        map.put("key", "value");
        assertThat(f.matches(map)).isFalse();

        map.clear();
        map.put("key", "not-value");
        assertThat(f.matches(map)).isTrue();

        map.clear();
        map.put("not-key", "value");
        assertThat(f.matches(map)).isTrue();
    }

    @Test
    public void testGetFilterNull() {
        try {
            FuchsiaUtils.getFilter(null);
            failBecauseExceptionWasNotThrown(InvalidFilterException.class);
        } catch (Exception e) {
            assertThat(e).isInstanceOf(InvalidFilterException.class)
                    .hasNoCause()
                    .hasMessageContaining("The given filter must be a String using LDAP syntax or an" +
                            "object org.osgi.framework.Filter");
        }
    }

    @Test
    public void testGetFilterOtherObject() {
        try {
            FuchsiaUtils.getFilter(new Object());
            failBecauseExceptionWasNotThrown(InvalidFilterException.class);
        } catch (Exception e) {
            assertThat(e).isInstanceOf(InvalidFilterException.class)
                    .hasNoCause()
                    .hasMessageContaining("The given filter must be a String using LDAP syntax or an" +
                            "object org.osgi.framework.Filter");
        }
    }

}
