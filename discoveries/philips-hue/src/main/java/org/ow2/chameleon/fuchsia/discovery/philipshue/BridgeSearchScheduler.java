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

import com.philips.lighting.hue.sdk.PHAccessPoint;
import com.philips.lighting.hue.sdk.PHBridgeSearchManager;
import com.philips.lighting.hue.sdk.PHHueSDK;
import com.philips.lighting.hue.sdk.PHSDKListener;
import com.philips.lighting.model.PHBridge;
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
    private Timer timer=new Timer("bridge-search-thread",true);
    private final Long poolingTime;
    private PHHueSDK philipsSDK;
    private PHBridgeSearchManager sm;

    public BridgeSearchScheduler(Long poolingTime){
        this.poolingTime=poolingTime;
        this.philipsSDK=PHHueSDK.getInstance();
        this.sm=(PHBridgeSearchManager) philipsSDK.getSDKService(PHHueSDK.SEARCH_BRIDGE);
    }

    public void activate(){
        run();
    }

    public void desactivate(){
        LOG.debug("Stopping philips bridge search.");
        timer.cancel();
    }

    public synchronized void searchFinished() {
        LOG.debug("Search finished. New search scheduled to run in {} ms.",poolingTime);
        timer.schedule(new BridgeSearchScheduler(this.poolingTime),poolingTime);

    }

    public void onCacheUpdated(int i, PHBridge phBridge) {
        //searchFinished();
    }

    public void onBridgeConnected(PHBridge phBridge) {
        //searchFinished();
    }

    public void onAuthenticationRequired(PHAccessPoint phAccessPoint) {
        //searchFinished();
    }

    public void onAccessPointsFound(List<PHAccessPoint> phAccessPoints) {
        searchFinished();
    }

    public void onError(int i, String s) {

        /**
         * 1157 is the code when no notification will be done and a new search should be scheduled, but we are dispatching a new search for anycase
         */
        searchFinished();

    }

    public void onConnectionResumed(PHBridge phBridge) {
        //searchFinished();
    }

    public void onConnectionLost(PHAccessPoint phAccessPoint) {
        //searchFinished();
    }

    @Override
    public void run() {
        LOG.debug("Searching for Hue..");
        sm.search(true, false, false);
    }
}
