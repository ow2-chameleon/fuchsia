package org.ow2.chameleon.fuchsia.discovery.filebased;

import org.apache.felix.ipojo.annotations.*;
import org.osgi.framework.BundleContext;
import org.ow2.chameleon.fuchsia.core.component.AbstractDiscoveryComponent;
import org.ow2.chameleon.fuchsia.core.component.DiscoveryService;
import org.ow2.chameleon.fuchsia.core.declaration.Constants;
import org.ow2.chameleon.fuchsia.core.declaration.ImportDeclaration;
import org.ow2.chameleon.fuchsia.core.declaration.ImportDeclarationBuilder;
import org.ow2.chameleon.fuchsia.discovery.filebased.monitor.ImporterDeployer;
import org.ow2.chameleon.fuchsia.discovery.filebased.monitor.DirectoryMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.apache.felix.ipojo.Factory.INSTANCE_NAME_PROPERTY;

/**
 * This component instantiate a directory monitor (initially pointed to a directory in chameleon called "load/import") that reads all file placed there (as property files)
 * and publishes an {@link org.ow2.chameleon.fuchsia.core.declaration.ImportDeclaration}
 *
 * @author jeremy.savonet@gmail.com
 * @author botelho (at) imag.fr
 * @author morgan.martinet@imag.fr
 */

@Component
@Provides(specifications = {DiscoveryService.class, ImporterDeployer.class})
public class FileBasedDiscoveryImportBridge extends AbstractDiscoveryComponent implements ImporterDeployer {

    @ServiceProperty(name = INSTANCE_NAME_PROPERTY)
    private String name;

    @ServiceProperty(name = FileBasedDiscoveryConstants.FILEBASED_DISCOVERY_IMPORT_PROPERTY_KEY_MONITORED_DIR_KEY, value = FileBasedDiscoveryConstants.FILEBASED_DISCOVERY_IMPORT_PROPERTY_KEY_MONITORED_DIR_VALUE)
    private String monitoredImportDirectory;

    @ServiceProperty(name = FileBasedDiscoveryConstants.FILEBASED_DISCOVERY_PROPERTY_POLLING_TIME_KEY, value = FileBasedDiscoveryConstants.FILEBASED_DISCOVERY_PROPERTY_POLLING_TIME_VALUE)
    private Long pollingTime;

    private final Map<String, ImportDeclaration> importDeclarationsFile = new HashMap<String, ImportDeclaration>();

    public FileBasedDiscoveryImportBridge(BundleContext bundleContext) {
        super(bundleContext);
    }

    @Validate
    public void start() {
        super.start();
        startMonitorDirectory(monitoredImportDirectory, pollingTime);
        getLogger().info("Filebased Import discovery up and running.");
    }

    private void startMonitorDirectory(String directory, Long poolTime) {
        try {
            DirectoryMonitor dm = new DirectoryMonitor(directory, pollingTime, ImporterDeployer.class.getName());
            dm.start(getBundleContext());
        } catch (Exception e) {
            getLogger().error("Failed to start "+DirectoryMonitor.class.getName()+" for the directory "+directory+" and polling time",e);
        }
    }

    @Invalidate
    public void stop() {
        super.stop();
        getLogger().info("Filebased Import discovery  stopped.");
    }

    @Override
    public Logger getLogger() {
        return LoggerFactory.getLogger(this.getClass());
    }

    public String getName() {
        return name;
    }

    public boolean accept(File file) {

        return !file.exists()||(!file.isHidden()&&file.isFile());

    }

    private Properties parseFile(File file) throws Exception {
        Properties properties = new Properties();
        InputStream is = null;
        try {
            is = new FileInputStream(file);
            properties.load(is);
        } catch (Exception e) {
            throw new Exception(String.format("Error reading import declaration file %s", file.getAbsoluteFile()),e);
        } finally {
            if(is!=null){
                is.close();
            }
        }

        if (!properties.containsKey(Constants.ID)) {
            throw new Exception(String.format("File %s is not a correct import declaration, needs to contains an id property", file.getAbsoluteFile()));
        }
        return properties;
    }

    public void onFileCreate(File file) {
        getLogger().info("New file detected : {}", file.getAbsolutePath());
        try {
            Properties properties = parseFile(file);
            HashMap<String, Object> metadata = new HashMap<String, Object>();
            for (Map.Entry<Object, Object> element : properties.entrySet()) {
                Object replacedObject = metadata.put(element.getKey().toString(), element.getValue());
                if (replacedObject != null) {
                    getLogger().warn("ImportDeclaration: replacing metadata key {}, that contained the value {} by the new value {}", new Object[]{element.getKey(), replacedObject, element.getValue()});
                }
            }
            ImportDeclaration declaration = createAndRegisterImportDeclaration(metadata);
            importDeclarationsFile.put(file.getAbsolutePath(), declaration);
        } catch (Exception e) {
            getLogger().error(e.getMessage(),e);
        }
    }

    // FIXME : this have to be rechecked, this is an pessimist approach
    public void onFileChange(File file) {
        getLogger().info("File updated : {}", file.getAbsolutePath());
        onFileDelete(file);
        onFileCreate(file);
    }

    public void onFileDelete(File file) {
        getLogger().info("File removed : {}", file.getAbsolutePath());
        ImportDeclaration declaration = importDeclarationsFile.get(file.getAbsolutePath());

        if (declaration == null) return;

        if (importDeclarationsFile.remove(file.getAbsolutePath()) == null) {
            getLogger().error("Failed to unregister import declaration file mapping ({}),  it did not existed before.", file.getAbsolutePath());
        } else {
            getLogger().info("import declaration file mapping removed.");
        }
        try {
            unregisterImportDeclaration(declaration);
        } catch (IllegalStateException e) {
            getLogger().error("Failed to unregister import declaration file {},  it did not existed before.", declaration.getMetadata(),e);
        }
    }

    public void open(Collection<File> files) {
        for (File file : files) {
            onFileChange(file);
        }
    }

    public void close() {
    }

    private ImportDeclaration createAndRegisterImportDeclaration(HashMap<String, Object> metadata) {
        ImportDeclaration declaration = ImportDeclarationBuilder.fromMetadata(metadata).build();
        registerImportDeclaration(declaration);
        return declaration;
    }

}
