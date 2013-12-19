package org.ow2.chameleon.fuchsia.protobuffer.protoclass;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Validate;
import org.osgi.framework.BundleContext;

import java.util.Dictionary;
import java.util.Hashtable;

@Component
@Instantiate
public class RegisterBookService {

    private BundleContext context;

    public RegisterBookService(BundleContext context){
        this.context=context;
    }


    @Validate
    public void validate(){

        Dictionary serviceProperties=new Hashtable<String,Object>();

        context.registerService(new String[]{com.google.protobuf.Service.class.getName(),AddressBookProtos.AddressBookService.class.getName()},new AddressBookServiceImpl(),serviceProperties);

        System.out.println("Service class exported");

    }

}
