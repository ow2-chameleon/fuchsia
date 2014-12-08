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
package org.ow2.chameleon.fuchsia.discovery.philipshue;

import com.philips.lighting.hue.sdk.*;
import com.philips.lighting.model.PHBridge;
import com.philips.lighting.model.PHHueError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Schedules the bridge search, according to the polling time
 */
public class BridgeSearchScheduler extends TimerTask implements PHSDKListener {

    private static final Logger LOG = LoggerFactory.getLogger(BridgeSearchScheduler.class);
    private Timer timer;
    private final Long poolingTime;
    private PHHueSDK philipsSDK;
    private PHBridgeSearchManager sm;

    public BridgeSearchScheduler(Long poolingTime){
        this.poolingTime=poolingTime;
        this.timer =new Timer("bridge-search-thread",true);
        this.philipsSDK=PHHueSDK.getInstance();
        this.sm=(PHBridgeSearchManager) philipsSDK.getSDKService(PHHueSDK.SEARCH_BRIDGE);
    }

    private BridgeSearchScheduler(BridgeSearchScheduler original){
        this.poolingTime=original.poolingTime;
        this.timer =original.timer;
        this.philipsSDK=original.philipsSDK;
        this.sm=original.sm;
    }

    public void activate(){
        run();
    }

    public void desactivate(){
        LOG.trace("Stopping philips bridge search.");
        timer.cancel();
    }

    public synchronized void searchFinished() {
        LOG.trace("Search finished. New search scheduled to run in {} ms.", poolingTime);
        timer.schedule(new BridgeSearchScheduler(this),poolingTime);

    }

    public void onCacheUpdated(int i, PHBridge phBridge) {
        //searchFinished();
        LOG.trace("PhilipsDiscovery: onCacheUpdated");
    }

    public void onBridgeConnected(PHBridge phBridge) {
        //searchFinished();
        LOG.trace("PhilipsDiscovery: onBridgeConnected");
    }

    public void onAuthenticationRequired(PHAccessPoint phAccessPoint) {
        //searchFinished();
        LOG.trace("PhilipsDiscovery: onAuthenticationRequired");
    }

    public void onAccessPointsFound(List<PHAccessPoint> phAccessPoints) {
        LOG.trace("PhilipsDiscovery: onAccessPointsFound");
        searchFinished();
    }

    public void onError(int code, String s) {

        LOG.trace("PhilipsDiscovery: onError code {} message {}",code,s);

        if (code == PHHueError.BRIDGE_NOT_RESPONDING) {
            searchFinished();
        }
        else if (code == PHMessageType.PUSHLINK_BUTTON_NOT_PRESSED) {
            //This is fired every single second after the bridge detection (and which he is not authenticated)
        }
        else if (code == PHMessageType.PUSHLINK_AUTHENTICATION_FAILED) {
            searchFinished();
        }
        else if (code == PHMessageType.BRIDGE_NOT_FOUND) {
            searchFinished();
        }

    }

    public void onConnectionResumed(PHBridge phBridge) {

        LOG.trace("PhilipsDiscovery: onConnectionResumed {}",phBridge);
        //searchFinished();
    }

    public void onConnectionLost(PHAccessPoint phAccessPoint) {
        //searchFinished();
    }

    @Override
    public void run() {
        LOG.debug("Searching for Hue..");
        Boolean scanLocalNetwork=Boolean.getBoolean("philips.discovery.scanNetwork");

        if(scanLocalNetwork)
            LOG.debug("Network scan activated!");

        sm.search(true, false, scanLocalNetwork);
    }
}
