package org.ow2.chameleon.fuchsia.core.component.manager.test;

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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.osgi.framework.*;
import org.ow2.chameleon.fuchsia.core.component.AbstractImporterComponent;
import org.ow2.chameleon.fuchsia.core.component.manager.DeclarationBinder;
import org.ow2.chameleon.fuchsia.core.component.manager.LinkerManagement;
import org.ow2.chameleon.fuchsia.core.declaration.Declaration;
import org.ow2.chameleon.fuchsia.core.declaration.ImportDeclaration;
import org.ow2.chameleon.fuchsia.core.declaration.ImportDeclarationBuilder;
import org.ow2.chameleon.fuchsia.core.exceptions.BinderException;
import org.ow2.chameleon.fuchsia.core.exceptions.InvalidFilterException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashSet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.assertj.core.api.Assertions.failBecauseExceptionWasNotThrown;
import static org.mockito.Mockito.*;
import static org.ow2.chameleon.fuchsia.core.component.manager.DeclarationBinder.TARGET_FILTER_PROPERTY;

public class LinkerManagementTest {

    @Mock
    BundleContext bundleContext;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this); //initialize the object with mocks annotations
    }

    @After
    public void tearDown() {

    }

    @Test
    public void testInstantiation() {
        Filter filterD = null, filterDB = null;
        try {
            filterD = FrameworkUtil.createFilter("(id=dec1)");
            filterDB = FrameworkUtil.createFilter("(instance.name=myDB)");
        } catch (InvalidSyntaxException e) {
            fail("", e);
        }
        assertThat(filterD).isNotNull();
        assertThat(filterDB).isNotNull();
        LinkerManagement<Declaration, DeclarationBinder<Declaration>> lm = new LinkerManagement<Declaration, DeclarationBinder<Declaration>>(bundleContext, filterDB, filterD);

        ImportDeclaration dec = ImportDeclarationBuilder.empty().key("id").value("dec1").key("field").value("imyourtarget").build();
        ServiceReference decSRef = mock(ServiceReference.class);
        when(bundleContext.getService(decSRef)).thenReturn(dec);
        lm.getDeclarationsManager().add(decSRef);

        assertThat(lm.getDeclarationsManager().matched(decSRef)).isTrue();
        assertThat(lm.getMatchedDeclaration()).containsExactly(dec);

        ServiceReference bSRef = mock(ServiceReference.class);
        when(bSRef.getPropertyKeys()).thenReturn(new String[]{"instance.name", TARGET_FILTER_PROPERTY});
        when(bSRef.getProperty("instance.name")).thenReturn("myDB");
        when(bSRef.getProperty(TARGET_FILTER_PROPERTY)).thenReturn("(field=imyourtarget)");
        DeclarationBinder db = spy(new SimpleImporter());

        when(bundleContext.getService(bSRef)).thenReturn(db);
        try {
            lm.getBindersManager().add(bSRef);
        } catch (InvalidFilterException e) {
            fail("", e);
        }
        assertThat(lm.getBindersManager().matched(bSRef)).isTrue();
        assertThat(lm.getMatchedBinderServiceRef()).containsExactly(bSRef);
        assertThat(lm.canBeLinked(dec, bSRef)).isTrue();
        assertThat(lm.link(dec, bSRef)).isTrue();
        try {
            verify(db).addDeclaration(dec);
        } catch (BinderException e) {
            fail("", e);
        }

        when(bSRef.getProperty("instance.name")).thenReturn("myDB2");
        try {
            lm.getBindersManager().modified(bSRef);
        } catch (InvalidFilterException e) {
            fail("", e);
        }
        if(lm.getBindersManager().matched(bSRef)){
            fail("");
        }
        lm.getBindersManager().removeLinks(bSRef);
        assertThat(lm.getBindersManager().matched(bSRef)).isFalse();
        assertThat(lm.getMatchedBinderServiceRef()).isEmpty();

        try {
            verify(db).removeDeclaration(dec);
        } catch (BinderException e) {
            fail("", e);
        }

        try {
            assertThat(lm.unlink(dec, bSRef)).isFalse();
            failBecauseExceptionWasNotThrown(IllegalStateException.class);
        }catch (IllegalStateException e){
            assertThat(e).hasNoCause();
        }

        lm.getBindersManager().remove(bSRef);
        lm.getDeclarationsManager().remove(decSRef);
    }

    public class SimpleImporter extends AbstractImporterComponent {

        private final Logger LOG = LoggerFactory.getLogger(SimpleImporter.class);

        private final Collection<ImportDeclaration> decs = new HashSet<ImportDeclaration>();

        @Override
        protected void useImportDeclaration(ImportDeclaration importDeclaration) {
            decs.add(importDeclaration);
        }

        @Override
        protected void denyImportDeclaration(ImportDeclaration importDeclaration) {
            decs.remove(importDeclaration);
        }

        public int nbProxies() {
            return decs.size();
        }

        public String getName() {
            return "simpleImporter";
        }

        @Override
        public void stop() {
            super.stop();
        }
    }
}
