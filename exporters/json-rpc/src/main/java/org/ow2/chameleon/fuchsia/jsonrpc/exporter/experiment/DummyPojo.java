package org.ow2.chameleon.fuchsia.jsonrpc.exporter.experiment;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;

/**
 * Created with IntelliJ IDEA.
 * User: jnascimento
 * Date: 27/01/14
 * Time: 10:17
 * To change this template use File | Settings | File Templates.
 */
@Component
@Instantiate(name = "DummyPojoInstance")
@Provides
public class DummyPojo implements DummyIface {

    public void helloworld(String value){

        System.out.println("hello "+value);

    }

}
