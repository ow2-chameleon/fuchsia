/*
 * #%L
 * OW2 Chameleon - Fuchsia Framework
 * %%
 * Copyright (C) 2009 - 2014 OW2 Chameleon
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package org.ow2.chameleon.fuchsia.discovery.knx;

import org.ow2.chameleon.fuchsia.discovery.knx.listener.KNXGatewaySearchListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tuwien.auto.calimero.exception.KNXException;
import tuwien.auto.calimero.knxnetip.Discoverer;
import tuwien.auto.calimero.knxnetip.KNXnetIPConnection;
import tuwien.auto.calimero.knxnetip.servicetype.SearchResponse;

import java.util.Set;

public class UpdateGatewaysThreads extends Thread {

    private Logger logger= LoggerFactory.getLogger(UpdateGatewaysThreads.class);

    private Set response;
    private Discoverer disc;
    private boolean active=true;
    private KNXGatewaySearchListener listener;


    public UpdateGatewaysThreads(final Set response,final KNXGatewaySearchListener listener){
        super.setDaemon(true);
        this.response=response;
        this.listener=listener;
        try {
            this.disc=new Discoverer(KNXnetIPConnection.IP_PORT,false);
        } catch (KNXException e) {
            logger.error("Failed instantiating discovery",e);
        }
    }

    public void run(){

        logger.info("Starting searching for KNX gateway ..");

        do {

            try {

                logger.info("Calimero: scanning for gateway..");

                disc.startSearch(2000,true);

                logger.info("Calimero: scanning finished");

                logger.info("Calimero: {} responses",disc.getSearchResponses().length);

                for (SearchResponse sr : disc.getSearchResponses()) {

                    logger.info("Found gateway on {} in port {}", sr.getControlEndpoint().getAddress().toString(), sr.getControlEndpoint().getPort());

                    if(!response.contains(sr) && listener!=null){
                        logger.info("new gateway found {}",sr.getDevice().getSerialNumberString());
                        listener.gatewayFound(sr);
                    }

                    response.add(sr);
                }



            } catch (KNXException e) {
                logger.error("Failed in performing knx gateway search",e);
            } finally {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    //This shouldn't fail
                }
            }


        }while(active);

    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
        if(!active) disc.stopSearch();
    }
}