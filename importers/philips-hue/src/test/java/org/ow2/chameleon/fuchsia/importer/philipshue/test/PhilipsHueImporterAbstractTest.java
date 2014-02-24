package org.ow2.chameleon.fuchsia.importer.philipshue.test;

import com.philips.lighting.model.PHBridge;
import com.philips.lighting.model.PHLight;
import org.fest.reflect.core.Reflection;
import org.junit.Before;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.packageadmin.PackageAdmin;
import org.ow2.chameleon.fuchsia.importer.philipshue.PhilipsHueImporter;

import java.util.Dictionary;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public abstract class PhilipsHueImporterAbstractTest {

    @Mock
    protected BundleContext context;
    @Mock
    Bundle bundle;
    @Mock
    PHLight light;
    @Mock
    PHBridge bridge;
    @Mock
    ServiceRegistration lightServiceRegistration;
    @Mock
    ServiceRegistration bridgeServiceRegistration;
    @Mock
    PackageAdmin packageAdmin;
    @Mock
    ServiceReference serviceReference;
    @Mock
    ServiceReference packageAdminServiceReference;

    PhilipsHueImporter importer;

    @Before
    public void validate(){
        MockitoAnnotations.initMocks(this);
        importer=spy(Reflection.constructor().withParameterTypes(BundleContext.class).in(PhilipsHueImporter.class).newInstance(context));
        setupInterceptors();
    }

    protected void setupInterceptors(){

        when(context.getServiceReference(PackageAdmin.class.getName())).thenReturn(packageAdminServiceReference);
        when(context.getService(packageAdminServiceReference)).thenReturn(packageAdmin);
        when(context.getBundle()).thenReturn(bundle);
        when(serviceReference.getProperty(org.osgi.framework.Constants.SERVICE_ID)).thenReturn(1l);
        when(context.registerService(eq(PHLight.class.getName()), anyObject(), any(Dictionary.class))).thenReturn(lightServiceRegistration);
        when(context.registerService(eq(PHBridge.class.getName()), anyObject(), any(Dictionary.class))).thenReturn(bridgeServiceRegistration);

    }

}
