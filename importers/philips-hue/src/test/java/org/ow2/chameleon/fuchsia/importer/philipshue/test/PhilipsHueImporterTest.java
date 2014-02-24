package org.ow2.chameleon.fuchsia.importer.philipshue.test;

import com.philips.lighting.model.PHBridge;
import com.philips.lighting.model.PHLight;
import junit.framework.Assert;
import org.fest.reflect.reference.TypeRef;
import org.junit.Test;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.ow2.chameleon.fuchsia.core.declaration.ImportDeclaration;
import org.ow2.chameleon.fuchsia.core.declaration.ImportDeclarationBuilder;
import org.ow2.chameleon.fuchsia.core.exceptions.BinderException;
import org.ow2.chameleon.fuchsia.importer.philipshue.PhilipsHueImporter;

import java.util.HashMap;
import java.util.Map;

import static org.fest.reflect.core.Reflection.field;
import static org.mockito.Mockito.*;

public class PhilipsHueImporterTest extends PhilipsHueImporterAbstractTest{

    private HashMap<String, Object> generateValidMetadata(String id){

        HashMap<String, Object> metadata = new HashMap<String, Object>();
        metadata.put("id",id);
        metadata.put("discovery.philips.device.name", "L1");
        metadata.put("discovery.philips.device.type", PHLight.class.getName());
        metadata.put("discovery.philips.device.object", light);
        metadata.put("discovery.philips.bridge.type", PHBridge.class.getName());
        metadata.put("discovery.philips.bridge.object", bridge);

        return metadata;
    }

    @Test
    public void checkDeclarationTurnsHandledAfterCall() throws BinderException {

        String philipsId="L1-ID";

        ImportDeclaration declaration = spy(ImportDeclarationBuilder.fromMetadata(generateValidMetadata(philipsId)).build());
        importer.registration(serviceReference);
        declaration.bind(serviceReference);

        importer.useDeclaration(declaration);

        verify(declaration,times(1)).handle(serviceReference);

    }

    @Test
    public void checkPoolOfLampBridgewereFilled() throws BinderException {

        String philipsId="L1-ID";

        ImportDeclaration declaration = spy(ImportDeclarationBuilder.fromMetadata(generateValidMetadata(philipsId)).build());
        importer.registration(serviceReference);
        declaration.bind(serviceReference);

        importer.useDeclaration(declaration);

        Map<String,ServiceRegistration> lamps=field("lamps").ofType(new TypeRef<Map<String,ServiceRegistration>>() {}).in(importer).get();
        Map<String,ServiceRegistration> bridges=field("bridges").ofType(new TypeRef<Map<String,ServiceRegistration>>() {}).in(importer).get();

        Assert.assertTrue(lamps.size() == 1);
        Assert.assertEquals(lamps.get(philipsId),lightServiceRegistration);

        Assert.assertTrue(bridges.size()==1);
        Assert.assertEquals(bridges.get(philipsId),bridgeServiceRegistration);

    }

    @Test
    public void rightExceptionHasBeenThrownInAbsenceOfProperty() {
        PhilipsHueImporter importer=spy(new PhilipsHueImporter(context));
        ServiceReference serviceReference=mock(ServiceReference.class);
        PHLight light=mock(PHLight.class);
        PHBridge bridge=mock(PHBridge.class);
        HashMap<String, Object> metadata = new HashMap<String, Object>();
        metadata.put("id", light.getIdentifier());
        ImportDeclaration declaration = ImportDeclarationBuilder.fromMetadata(metadata).build();
        importer.registration(serviceReference);

        try {
            importer.useDeclaration(declaration);
            Assert.fail("An exception "+BinderException.class.getSimpleName()+" should have been thrown.");
        }catch (BinderException be){
            //If this exception was reached, everything is OK
        }

    }

}
