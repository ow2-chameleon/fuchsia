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
package org.ow2.chameleon.fuchsia.importer.knx.util;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.ow2.chameleon.fuchsia.importer.knx.dao.KNXLink;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tuwien.auto.calimero.exception.KNXException;
import tuwien.auto.calimero.knxnetip.KNXnetIPConnection;
import tuwien.auto.calimero.link.KNXNetworkLink;
import tuwien.auto.calimero.link.KNXNetworkLinkIP;
import tuwien.auto.calimero.link.medium.TPSettings;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

@Component
@Instantiate
@Provides
public class KNXLinkManager {

    private static final Logger LOG = LoggerFactory.getLogger(KNXLinkManager.class);

    Map<KNXLink,KNXNetworkLink> linkPool=new HashMap<KNXLink, KNXNetworkLink>();

    public KNXNetworkLink getLink(String local,String gateway){

        KNXLink link=new KNXLink(local,gateway);

        if(linkPool.get(link)==null){
            try {
                linkPool.put(link,createLink(local, gateway));
            } catch (KNXException e) {
                e.printStackTrace();
                LOG.error("KNX: Failed to create link from inet local {} to {}",local,gateway);
            }
        }

        KNXNetworkLink knxLink=linkPool.get(link);

        return knxLink;

    }

    private static InetSocketAddress createLocalSocket(InetAddress host, Integer port) {
        final int p = port != null ? port.intValue() : 0;
        try {
            return host != null ? new InetSocketAddress(host, p) : p != 0
                    ? new InetSocketAddress(InetAddress.getLocalHost(), p) : null;
        } catch (final UnknownHostException e) {
            throw new IllegalArgumentException("failed to create local host "
                    + e.getMessage());
        }
    }

    private KNXNetworkLink createLink(String localhostIface,String gatewayIface) throws KNXException {
        try {
            InetAddress localhost = InetAddress.getByName(localhostIface);
            InetAddress gatewayHost = InetAddress.getByName(gatewayIface);
            final InetSocketAddress local = createLocalSocket(localhost, null);
            final InetSocketAddress host = new InetSocketAddress(gatewayHost,
                    KNXnetIPConnection.IP_PORT);
            final int mode = KNXNetworkLinkIP.TUNNEL;

            LOG.info("Mode {} local {} host {}",new Object[]{mode,local,host});

            return new KNXNetworkLinkIP(mode, local, host, false,
                    TPSettings.TP1);

        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void disconnect(){

        for(Map.Entry<KNXLink,KNXNetworkLink> entry:linkPool.entrySet()){
            LOG.info("Disconnecting from {}",entry.getKey().getGateway());
            entry.getValue().close();
            LOG.info("Disconnected from {}",entry.getKey().getGateway());
        }

    }

}
