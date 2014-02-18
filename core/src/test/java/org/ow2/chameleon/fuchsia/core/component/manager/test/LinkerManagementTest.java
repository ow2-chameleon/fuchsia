package org.ow2.chameleon.fuchsia.core.component.manager.test;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.osgi.framework.*;
import org.ow2.chameleon.fuchsia.core.component.manager.DeclarationBinder;
import org.ow2.chameleon.fuchsia.core.component.manager.LinkerManagement;
import org.ow2.chameleon.fuchsia.core.declaration.Declaration;
import org.ow2.chameleon.fuchsia.core.declaration.ImportDeclaration;
import org.ow2.chameleon.fuchsia.core.declaration.ImportDeclarationBuilder;
import org.ow2.chameleon.fuchsia.core.exceptions.BinderException;
import org.ow2.chameleon.fuchsia.core.exceptions.InvalidFilterException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
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
        LinkerManagement<Declaration, DeclarationBinder<Declaration>> lm = new LinkerManagement<Declaration, DeclarationBinder<Declaration>>(bundleContext, filterD, filterDB);

        ImportDeclaration dec = ImportDeclarationBuilder.empty().key("id").value("dec1").key("field").value("imyourtarget").build();
        ServiceReference decSRef = mock(ServiceReference.class);
        when(bundleContext.getService(decSRef)).thenReturn(dec);
        lm.declarationsManager.add(decSRef);


        ServiceReference bSRef = mock(ServiceReference.class);
        when(bSRef.getPropertyKeys()).thenReturn(new String[]{"instance.name", TARGET_FILTER_PROPERTY});
        when(bSRef.getProperty("instance.name")).thenReturn("myDB");
        when(bSRef.getProperty(TARGET_FILTER_PROPERTY)).thenReturn("(field=imyourtarget)");
        DeclarationBinder db = mock(DeclarationBinder.class);

        when(bundleContext.getService(bSRef)).thenReturn(db);
        try {
            lm.bindersManager.add(bSRef);
        } catch (InvalidFilterException e) {
            fail("", e);
        }
        assertThat(lm.canBeLinked(dec, bSRef)).isTrue();
        lm.link(dec, bSRef);
        try {
            verify(db).addDeclaration(dec);
        } catch (BinderException e) {
            fail("", e);
        }

        lm.unlink(dec, bSRef);
        try {
            verify(db).removeDeclaration(dec);
        } catch (BinderException e) {
            fail("", e);
        }
    }
}
