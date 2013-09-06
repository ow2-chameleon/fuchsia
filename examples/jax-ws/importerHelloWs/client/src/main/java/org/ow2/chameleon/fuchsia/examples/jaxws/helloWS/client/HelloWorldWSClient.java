package org.ow2.chameleon.fuchsia.examples.jaxws.helloWS.client;

import org.apache.felix.ipojo.annotations.*;
import org.osgi.framework.ServiceReference;
import org.ow2.chameleon.fuchsia.examples.jaxws.helloWS.HelloWorldWS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created with IntelliJ IDEA.
 * User: jeremy
 * Date: 14/08/13
 * Time: 14:46
 * To change this template use File | Settings | File Templates.
 */
@Component
@Instantiate
public class HelloWorldWSClient {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private HelloWorldWS m_helloWorldWS;

    @Bind (id="helloWs",optional = true)
    public void bindGreeterService(HelloWorldWS helloWorldWS, ServiceReference ref) {
        logger.debug("Bind helloWS service !!!");

        m_helloWorldWS = helloWorldWS;
        useService(m_helloWorldWS);
    }

    @Unbind(id="helloWs")
    public void unbindGreeterService(HelloWorldWS helloWorldWS) {
        logger.debug("Unbind helloWS service !!!");
        m_helloWorldWS = null;
    }

    @Validate
    public void start() {
        logger.debug("Start HelloWS client !!!");
    }

    @Invalidate
    public void stop() {
        logger.debug("Stop HelloWS client !!!");
        m_helloWorldWS = null;
    }

    protected void useService(HelloWorldWS helloWS) {
        helloWS.sayHello("World !!!");
        helloWS.sayGoodBye("World !!!");
    }
}
