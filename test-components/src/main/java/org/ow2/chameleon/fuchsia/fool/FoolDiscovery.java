package org.ow2.chameleon.fuchsia.fool;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.osgi.framework.BundleContext;
import org.ow2.chameleon.fuchsia.core.component.AbstractDiscoveryComponent;
import org.ow2.chameleon.fuchsia.core.component.DiscoveryService;
import org.ow2.chameleon.fuchsia.core.declaration.ImportDeclaration;
import org.ow2.chameleon.fuchsia.core.declaration.ImportDeclarationBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ScheduledThreadPoolExecutor;

/**
 * Because it's a fool factory it just create and destroy ImportDeclaration using threads
 * and pre-created ImportDeclarations.
 * It don't really get what it does, but it's ok.
 */
@Component(name = "Fuchsia-FoolDiscovery-Factory")
@Provides(specifications={DiscoveryService.class})
@Instantiate(name = "Fuchsia-FoolDiscovery")
public class FoolDiscovery extends AbstractDiscoveryComponent {


    private final ScheduledThreadPoolExecutor pool_register = new ScheduledThreadPoolExecutor(1);
    private final ScheduledThreadPoolExecutor pool_unregister = new ScheduledThreadPoolExecutor(1);

    private final List<ImportDeclaration> importDeclarations = new ArrayList<ImportDeclaration>();
    private Integer index_importDeclaration_register;
    private Integer index_importDeclaration_unregister;

    public FoolDiscovery(BundleContext bundleContext) {
        super(bundleContext);
        System.err.println("Creating fool discovery !");
    }


    public void start() {
        System.err.println("Start fool discovery !");
        for (int i = 0; i < 2; i++) {
            Map<String, Object> metadata = new HashMap<String, Object>();
            metadata.put("fool", "fool");
            metadata.put("fool-number", i);
            importDeclarations.add(ImportDeclarationBuilder.create().withMetadata(metadata).build());
        }
        index_importDeclaration_register = -1;
        index_importDeclaration_unregister = -1;

//        pool_register.execute(new Registrator());
//        pool_unregister.execute(new Unregistrator());

    }

    public void stop() {
//        pool_unregister.shutdown();
//        pool_register.shutdown();

        importDeclarations.clear();
    }


    protected class Registrator implements Runnable {

        Random random = new Random();

        public void run() {
            while (index_importDeclaration_register < importDeclarations.size()) {
                try {
                    Thread.sleep((random.nextInt(5) + 1) * 10000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                registerImportDeclaration(importDeclarations.get(index_importDeclaration_register + 1));
                index_importDeclaration_register = index_importDeclaration_register + 1;
            }

        }
    }

    protected class Unregistrator implements Runnable {

        Random random = new Random();

        public void run() {
            while (index_importDeclaration_unregister < importDeclarations.size()) {
                while (index_importDeclaration_unregister == index_importDeclaration_register) {
                    try {
                        Thread.sleep((random.nextInt(5) + 1) * 1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                unregisterImportDeclaration(importDeclarations.get(index_importDeclaration_unregister + 1));
                index_importDeclaration_unregister = index_importDeclaration_unregister + 1;
            }

        }
    }

}
