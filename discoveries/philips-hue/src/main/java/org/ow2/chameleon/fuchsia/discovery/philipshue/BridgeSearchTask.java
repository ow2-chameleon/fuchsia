package org.ow2.chameleon.fuchsia.discovery.philipshue;

import java.util.TimerTask;

public class BridgeSearchTask extends TimerTask {

    private PhilipsHueBridgeDiscovery phi;

    public BridgeSearchTask(PhilipsHueBridgeDiscovery phi){
        this.phi=phi;
    }

    @Override
    public void run() {
        phi.searchForBridges();
    }
}
