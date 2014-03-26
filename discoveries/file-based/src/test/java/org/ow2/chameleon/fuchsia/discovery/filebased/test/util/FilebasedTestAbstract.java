package org.ow2.chameleon.fuchsia.discovery.filebased.test.util;

import junit.framework.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.osgi.framework.BundleContext;
import org.ow2.chameleon.fuchsia.discovery.filebased.AbstractFileBasedDiscovery;
import org.ow2.chameleon.fuchsia.discovery.filebased.FileBasedDiscoveryConstants;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.Properties;

import static org.fest.reflect.core.Reflection.field;
import static org.fest.reflect.core.Reflection.method;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public abstract class FilebasedTestAbstract <D extends AbstractFileBasedDiscovery> {

    protected D discovery;

    @Rule
    public TemporaryFolder tempFolder =new TemporaryFolder();

    @Mock
    protected BundleContext context;

    @Before
    public void before(){

        MockitoAnnotations.initMocks(this);

        init();

        initGenericMocks();

    }

    public abstract void init();

    private void initGenericMocks(){

        field("name").ofType(String.class).in(discovery).set("instance-name");
        field("pollingTime").ofType(Long.class).in(discovery).set(Long.valueOf(FileBasedDiscoveryConstants.FILEBASED_DISCOVERY_PROPERTY_POLLING_TIME_VALUE));
        method("start").in(discovery).invoke();

    }

    protected File getTempFile(String name) throws IOException {

        File file= tempFolder.newFile(name);

        Properties props = new Properties();
        props.setProperty("id", "my-id");

        OutputStream out = new FileOutputStream( file );
        props.store(out,"");
        out.close();

        return file;

    }

    @Test
    public void createFile() throws IOException {

        File file=getTempFile("newfile.txt");

        discovery.onFileCreate(file);

        Map maps=field("declarationsFiles").ofType(Map.class).in(discovery).get();

        Assert.assertEquals(1, maps.size());

    }

    @Test
    public void removeFile() throws IOException {

        File file=getTempFile("filetoberemoved.txt");

        discovery.onFileCreate(file);

        Map maps=field("declarationsFiles").ofType(Map.class).in(discovery).get();

        Assert.assertEquals(1,maps.size());

        discovery.onFileDelete(file);

        Assert.assertEquals(0,maps.size());

    }

    @Test
    public void gracefulStop() throws IOException {
        File file=getTempFile("filetoberemoved.txt");
        discovery.onFileCreate(file);
        Map maps=field("declarationsFiles").ofType(Map.class).in(discovery).get();
        Assert.assertEquals(1,maps.size());
        method("stop").in(discovery).invoke();
        Assert.assertEquals(0,maps.size());
    }

    @Test
    public void shouldNotAcceptHiddenFiled(){

        File invalidFile=mock(File.class);
        when(invalidFile.exists()).thenReturn(true);
        when(invalidFile.isFile()).thenReturn(true);
        when(invalidFile.isHidden()).thenReturn(true);

        File validFile=mock(File.class);
        when(validFile.exists()).thenReturn(true);
        when(validFile.isFile()).thenReturn(true);
        when(validFile.isHidden()).thenReturn(false);

        Boolean resultValid=discovery.accept(validFile);
        Boolean resultInvalid=discovery.accept(invalidFile);

        Assert.assertTrue(resultValid);
        Assert.assertFalse(resultInvalid);
    }

}
