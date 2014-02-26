package org.ow2.chameleon.fuchsia.discovery.philipshue;

import com.philips.lighting.hue.sdk.PHAccessPoint;
import com.philips.lighting.hue.sdk.PHBridgeSearchManager;
import com.philips.lighting.hue.sdk.PHHueSDK;
import com.philips.lighting.hue.sdk.PHSDKListener;
import com.philips.lighting.hue.sdk.connection.impl.PHBridgeInternal;
import com.philips.lighting.hue.sdk.exception.PHHueException;
import com.philips.lighting.model.PHBridge;
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
import java.util.Map;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import static org.apache.felix.ipojo.Factory.INSTANCE_NAME_PROPERTY;

@Component
@Provides(specifications = {DiscoveryService.class})
@Instantiate
public class PhilipsHueDiscovery extends AbstractDiscoveryComponent implements PHSDKListener, Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(PhilipsHueDiscovery.class);

    @ServiceProperty(name = INSTANCE_NAME_PROPERTY)
    private String name;

    @ServiceProperty(name = "philips.hue.discovery.pooling", value = "5000")
    private Long pollingTime;

    private PHAccessPoint ap;

    private boolean isRunning = false;

    private Preferences preferences = Preferences.userRoot().node(this.getClass().getName());

    public PhilipsHueDiscovery(BundleContext bundleContext) {
        super(bundleContext);
    }

    @Validate
    public void start() {

        LOG.info("Philips Hue discovery is up and running.");

        PHHueSDK.getInstance().getNotificationManager().registerSDKListener(this);

        isRunning = true;

        Thread t1 = new Thread(this);
        t1.setDaemon(true);
        t1.start();
    }

    @Invalidate
    public void stop() {
        isRunning = false;
    }

    public String getName() {
        return name;
    }


    public void onCacheUpdated(int i, PHBridge phBridge) {
    }

    public void onBridgeConnected(PHBridge phBridge) {
        PHHueSDK.getInstance().setSelectedBridge(phBridge);
        PHHueSDK.getInstance().enableHeartbeat(phBridge, pollingTime);
        LOG.info("bridge connected");
    }

    public void onAuthenticationRequired(PHAccessPoint phAccessPoint) {
        LOG.warn("authentication required, you have 30 seconds to push the button on the bridge");

        PHHueSDK.getInstance().startPushlinkAuthentication(phAccessPoint);
    }

    public void onAccessPointsFound(List<PHAccessPoint> phAccessPoints) {
        ap = phAccessPoints.get(phAccessPoints.size() - 1);
    }

    public void onError(int i, String s) {
        //everytime we dispatch a search and it doesnt find a bridge, is considered as an error
    }

    public void onConnectionResumed(PHBridge phBridge) {
    }

    public void onConnectionLost(PHAccessPoint phAccessPoint) {
        LOG.info("connection lost");
    }

    public void run() {

        PHBridgeSearchManager sm = (PHBridgeSearchManager) PHHueSDK.getInstance().getSDKService(PHHueSDK.SEARCH_BRIDGE);

        while (isRunning) {

            sm.search(true, false);

            if (ap != null) {
                final String bridgeUsernameKey = "username";

                String username = preferences.get(bridgeUsernameKey, null);

                if (username == null) {
                    username = PHBridgeInternal.generateUniqueKey();
                    preferences.put(bridgeUsernameKey, username);
                    try {
                        preferences.flush();
                    } catch (BackingStoreException e) {
                        LOG.error("failed to store username in java preferences, this will force you to push the bridge button everytime to authenticate", e);
                    }
                }

                ap.setUsername(username);

                try {
                    PHHueSDK.getInstance().connect(ap);
                } catch (PHHueException e) {
                    LOG.debug("Failed to connect to the Philips Hue AP with the message {}", e.getMessage(), e);
                }

                PHBridge bridge = PHHueSDK.getInstance().getSelectedBridge();

                if (PHHueSDK.getInstance().getSelectedBridge() != null) {
                    PHBridgeResourcesCache cache = bridge.getResourceCache();
                    for (PHLight light : cache.getAllLights()) {
                        generateImportDeclaration(light, bridge);
                    }
                }
            }

            try {
                Thread.sleep(pollingTime);
            } catch (InterruptedException e) {
                LOG.error("failed to put in wait state", e);
            }


        }
    }

    private void generateImportDeclaration(PHLight light, PHBridge bridge) {

        Map<String, Object> metadata = new HashMap<String, Object>();

        metadata.put("id", light.getIdentifier());
        metadata.put("discovery.philips.device.name", light.getModelNumber());
        metadata.put("discovery.philips.device.type", light.getClass().getName());
        metadata.put("discovery.philips.device.object", light);
        metadata.put("discovery.philips.bridge.type", PHBridge.class.getName());
        metadata.put("discovery.philips.bridge.object", bridge);

        ImportDeclaration declaration = ImportDeclarationBuilder.fromMetadata(metadata).build();

        boolean found = false;
        for (ImportDeclaration im : super.getImportDeclarations()) {
            if (im.getMetadata().get("id").toString().equals(im.getMetadata().get("id").toString())) {
                found = true;
            }
        }

        if (!found) {
            super.registerImportDeclaration(declaration);
        }


    }
}
