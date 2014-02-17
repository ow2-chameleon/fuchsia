package org.ow2.chameleon.fuchsia.exporter.cxf.examples.base;


import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;

@Component
@Instantiate(name = "PojoSampleToBeExported")
@Provides
public class PojoSampleToBeExported implements PojoSampleToBeExportedIface {

    public void showMessage2(){
        System.out.println("ok");
    }

    public String getMessage2(){
        return "ok";
    }

}
