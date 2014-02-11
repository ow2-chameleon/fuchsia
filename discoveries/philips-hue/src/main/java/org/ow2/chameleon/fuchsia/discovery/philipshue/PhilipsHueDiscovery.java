package org.ow2.chameleon.fuchsia.discovery.philipshue;

import com.philips.lighting.hue.sdk.PHAccessPoint;
import com.philips.lighting.hue.sdk.PHBridgeSearchManager;
import com.philips.lighting.hue.sdk.PHHueSDK;
import com.philips.lighting.hue.sdk.PHSDKListener;
import com.philips.lighting.hue.sdk.connection.impl.PHBridgeInternal;
import com.philips.lighting.hue.sdk.exception.PHHueException;
import com.philips.lighting.model.PHBridge;
import com.philips.lighting.model.PHBridgeConfiguration;
import com.philips.lighting.model.PHBridgeResourcesCache;
import com.philips.lighting.model.PHLight;
import org.apache.felix.ipojo.annotations.*;
import org.osgi.framework.BundleContext;
import org.ow2.chameleon.fuchsia.core.component.AbstractDiscoveryComponent;
import org.ow2.chameleon.fuchsia.core.component.DiscoveryService;
import org.ow2.chameleon.fuchsia.core.declaration.ImportDeclaration;
import org.ow2.chameleon.fuchsia.core.declaration.ImportDeclarationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import static org.apache.felix.ipojo.Factory.INSTANCE_NAME_PROPERTY;

@Component()
@Provides(specifications = {DiscoveryService.class})
@Instantiate
public class PhilipsHueDiscovery extends AbstractDiscoveryComponent implements PHSDKListener, Runnable {

    @ServiceProperty(name = INSTANCE_NAME_PROPERTY)
    private String name;

    @ServiceProperty(name = "philips.hue.discovery.pooling",value = "5000")
    private Long DISCOVERY_POLLING_TIME;

    private Logger log = LoggerFactory.getLogger(this.getClass().getName());

    private PHAccessPoint ap;

    private boolean isRunning=false;

    private Preferences preferences=Preferences.userRoot().node(this.getClass().getName());

    public PhilipsHueDiscovery(BundleContext bundleContext) {
        super(bundleContext);
    }

    @Validate
    public void start() {

        log.info("Philips Hue discovery is up and running.");

        PHHueSDK.getInstance().getNotificationManager().registerSDKListener(this);

        isRunning=true;

        Thread t1=new Thread(this);
        t1.setDaemon(true);
        t1.start();

    }

    @Invalidate
    public void stop() {
        isRunning=false;
    }

    @Override
    public Logger getLogger() {
        return LoggerFactory.getLogger(this.getClass());
    }

    public String getName() {
        return name;
    }


    public void onCacheUpdated(int i, PHBridge phBridge) {
        //log.info("cache updated");
    }

    public void onBridgeConnected(PHBridge phBridge) {

        PHHueSDK.getInstance().setSelectedBridge(phBridge);
        PHHueSDK.getInstance().enableHeartbeat(phBridge, DISCOVERY_POLLING_TIME);
        log.info("bridge connected");

    }

    public void onAuthenticationRequired(PHAccessPoint phAccessPoint) {

        log.warn("authentication required, you have 30 seconds to push the button on the bridge");

        PHHueSDK.getInstance().startPushlinkAuthentication(phAccessPoint);
    }

    public void onAccessPointsFound(List<PHAccessPoint> phAccessPoints) {
        //log.info("access point found {}",phAccessPoints);

        ap=phAccessPoints.get(phAccessPoints.size()-1);

    }

    public void onError(int i, String s) {
        //everytime we dispatch a search and it doesnt find a bridge, is considered as an error
    }

    public void onConnectionResumed(PHBridge phBridge) {
        //log.info("connection resumed");
    }

    public void onConnectionLost(PHAccessPoint phAccessPoint) {
        log.info("connection lost");
    }

    public void run() {

        PHBridgeSearchManager sm = (PHBridgeSearchManager) PHHueSDK.getInstance().getSDKService(PHHueSDK.SEARCH_BRIDGE);

        while(isRunning){

            sm.search(true, false);

            if(ap!=null){

                try{
                    final String BRIDGE_USERNAME_KEY="username";

                    String username=preferences.get(BRIDGE_USERNAME_KEY,null);

                    if(username==null){
                        username=PHBridgeInternal.generateUniqueKey();
                        preferences.put(BRIDGE_USERNAME_KEY,username);
                        try {
                            preferences.flush();
                        } catch (BackingStoreException e) {
                            log.error("failed to store username in java preferences, this will force you to push the bridge button everytime to authenticate",e);
                        }
                    }

                    ap.setUsername(username);

                    PHHueSDK.getInstance().connect(ap);

                }catch(PHHueException e){
                    log.debug("Failed to connect to the Philips Hue AP with the message {}", e.getMessage(),e);
                }

                PHBridge bridge = PHHueSDK.getInstance().getSelectedBridge();

                if(PHHueSDK.getInstance().getSelectedBridge()!=null){

                    PHBridgeResourcesCache cache = bridge.getResourceCache();

                    for(PHLight light:cache.getAllLights()){

                        generateImportDeclaration(light,bridge);

                    }

                }

            }

            try {
                Thread.sleep(DISCOVERY_POLLING_TIME);
            } catch (InterruptedException e) {
                log.error("failed to put in wait state");
            }


        }
    }

    private void generateImportDeclaration(PHLight light,PHBridge bridge){

        HashMap<String, Object> metadata = new HashMap<String, Object>();

        metadata.put("id", light.getIdentifier());
        metadata.put("discovery.philips.device.name", light.getModelNumber());
        metadata.put("discovery.philips.device.type", light.getClass().getName());
        metadata.put("discovery.philips.device.object", light);
        metadata.put("discovery.philips.bridge.type", PHBridge.class.getName());
        metadata.put("discovery.philips.bridge.object", bridge);

        ImportDeclaration declaration = ImportDeclarationBuilder.fromMetadata(metadata).build();

        boolean found=false;
        for(ImportDeclaration im:super.getImportDeclarations()){
            if(im.getMetadata().get("id").toString().equals(im.getMetadata().get("id").toString())){
                found=true;
            }
        }

        if(!found){
            super.registerImportDeclaration(declaration);
        }



    }
}
