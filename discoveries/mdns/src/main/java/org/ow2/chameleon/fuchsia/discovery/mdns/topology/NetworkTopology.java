package org.ow2.chameleon.fuchsia.discovery.mdns.topology;

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

        List<InetAddress> addresses=new ArrayList<InetAddress>();

        try {
            for (NetworkInterface ni :  Collections.list(NetworkInterface.getNetworkInterfaces())) {

                for (InetAddress inetAddress : Collections.list(ni.getInetAddresses())) {
                    if(!this.useInetAddress(ni, inetAddress)) continue;
                    addresses.add(inetAddress);
                }
            }

            return addresses.toArray(new InetAddress[0]);

        } catch (SocketException e) {
            LOGGER.error("Socket exception",e);
        }

        return null;

    }

    public boolean useInetAddress(NetworkInterface networkInterface,
                                  InetAddress interfaceAddress) {

        if (interfaceAddress instanceof Inet6Address
            //TODO in the future hide localhost	|| !interfaceAddress.isLoopbackAddress()
                ) return false;

        return true;
    }


    public void lockInetAddress(InetAddress arg0) {
        //TODO: lock is unecessary for now
    }

    public void unlockInetAddress(InetAddress arg0) {
        //TODO: lock is unecessary for now
    }

}
