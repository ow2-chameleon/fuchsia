package org.ow2.chameleon.fuchsia.base.test.common.platform;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.packageadmin.PackageAdmin;
import org.ow2.chameleon.fuchsia.core.component.manager.DeclarationBinder;
import org.ow2.chameleon.fuchsia.core.declaration.Declaration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

/**
 * This class is responsible in filling the main field by to be used by the test, all basic needs from an importer/exporter are filled up with Mockito
 * @param <T> The declaration type (ImportDeclaration or ExportDeclaration)
 * @param <S> Importer or an Exporter
 */
public abstract class GenericImportExporterPlatformTest<T extends Declaration,S extends DeclarationBinder> {

    public static final Integer HTTP_PORT=8046;

    protected final Map<ServiceReference,Object> services=new HashMap<ServiceReference,Object>();

    protected final Map<String,Class> clazzes=new HashMap<String,Class>();

    @Mock
    protected BundleContext context;

    @Mock
    protected PackageAdmin packageAdmin;

    @Mock
    protected ServiceReference packageAdminServiceReference;

    @Mock
    protected Bundle bundle;

    protected S fuchsiaDeclarationBinder;

    @Mock
    protected ServiceReference fuchsiaDeclarationBinderServiceReference;


    public void initialize() throws Exception {

        MockitoAnnotations.initMocks(this);

        try {
            when(context.getBundle()).thenReturn(bundle);
            when(bundle.loadClass(anyString())).thenAnswer(new Answer<Object>() {
                public Object answer(InvocationOnMock invocation) throws Throwable {
                    return clazzes.get((String)invocation.getArguments()[0]);
                }
            });

            when(context.getServiceReference(PackageAdmin.class.getName())).thenReturn(packageAdminServiceReference);
            when(fuchsiaDeclarationBinderServiceReference.getProperty(org.osgi.framework.Constants.SERVICE_ID)).thenReturn(1l);
            when(context.getService(any(ServiceReference.class))).thenAnswer(new Answer<Object>() {
                public Object answer(InvocationOnMock invocation) throws Throwable {

                    ServiceReference sr=(ServiceReference)invocation.getArguments()[0];

                    return services.get(sr);
                }
            });
            when(context.getServiceReferences(any(Class.class), anyString())).then(new Answer<Object>() {
                public Object answer(InvocationOnMock invocation) throws Throwable {
                    ArrayList<ServiceReference> list=new ArrayList<ServiceReference>();
                    Class clazz= (Class) invocation.getArguments()[0];
                    for(Map.Entry<ServiceReference,Object> entry:services.entrySet()){

                        if(clazz==null || clazz.isInstance(entry.getValue()))
                            list.add(entry.getKey());

                    }
                    return list;

                }
            });
            when(context.getServiceReference(any(Class.class))).thenAnswer(new Answer<Object>() {
                public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                    Class clazz= (Class) invocationOnMock.getArguments()[0];
                    for(Map.Entry<ServiceReference,Object> entry:services.entrySet()){
                        if(clazz.isInstance(entry.getValue())){
                            return entry.getValue();
                        }
                    }
                    return null;
                }
            });
        } catch (InvalidSyntaxException e) {
            e.printStackTrace();
        }
    }

    protected void registerService(ServiceReference sr,Object object){
        services.put(sr,object);
    }

    protected void registerClass(Class clazz){
        clazzes.put(clazz.getName(),clazz);
    }

    public abstract List<T> getInvalidDeclarations();

    public abstract List<T> getValidDeclarations();


}
