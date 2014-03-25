package org.ow2.chameleon.fuchsia.discovery.mdns.topology;

/*
 * #%L
 * OW2 Chameleon - Fuchsia Discovery mDNS
 * %%
 * Copyright (C) 2009 - 2014 OW2 Chameleon
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jmdns.NetworkTopologyDiscovery;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NetworkTopology implements NetworkTopologyDiscovery {

    private static final Logger LOGGER = LoggerFactory.getLogger(NetworkTopology.class);

    public InetAddress[] getInetAddresses() {

        List<InetAddress> addresses = new ArrayList<InetAddress>();

        try {
            for (NetworkInterface ni : Collections.list(NetworkInterface.getNetworkInterfaces())) {

                for (InetAddress inetAddress : Collections.list(ni.getInetAddresses())) {
                    if (this.useInetAddress(ni, inetAddress)) {
                        addresses.add(inetAddress);
                    }
                }
            }

            return addresses.toArray(new InetAddress[addresses.size()]);

        } catch (SocketException e) {
            LOGGER.error("Socket exception", e);
        }

        return new InetAddress[0];
    }

    public boolean useInetAddress(NetworkInterface networkInterface, InetAddress interfaceAddress) {
        //TODO in the future hide localhost || !interfaceAddress.isLoopbackAddress()
        return !(interfaceAddress instanceof Inet6Address);
    }


    public void lockInetAddress(InetAddress arg0) {
        //TODO: lock is unecessary for now
    }

    public void unlockInetAddress(InetAddress arg0) {
        //TODO: lock is unecessary for now
    }

}
