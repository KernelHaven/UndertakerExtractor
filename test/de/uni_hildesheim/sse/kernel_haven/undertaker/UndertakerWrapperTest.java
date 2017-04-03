package de.uni_hildesheim.sse.kernel_haven.undertaker;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.IOException;

import org.junit.AfterClass;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import de.uni_hildesheim.sse.kernel_haven.util.Logger;

/**
 * Tests the undertaker wrapper.
 * 
 * @author Adam
 * @author Johannes
 */
public class UndertakerWrapperTest {

    private static final File RESOURCE_DIR = new File("testdata/tmp_res");
    
    private static final File SOURCE_DIR = new File("testdata");
    
    private UndertakerWrapper wrapper;
    
    /**
     * Initializes the logger.
     */
    @BeforeClass
    public static void beforeClass() {
        Logger.init();
    }
    
    /**
     * Clears the temporary resource directory.
     */
    @AfterClass
    public static void afterClass() {
        for (File file : RESOURCE_DIR.listFiles()) {
            file.delete();
        }
    }
    
    /**
     * Creates a new KbuildMinerWrapper for each test.
     * @throws IOException unwanted.
     */
    @Before
    public void setUp() throws IOException {
        wrapper = new UndertakerWrapper(RESOURCE_DIR, SOURCE_DIR, 50000);
    }
    
    /**
     * Tests whether undertaker correctly creates a CSV file.
     * 
     * @throws IOException unwanted. 
     */
    @Test
    public void testUndertakerRun() throws IOException {
        Assume.assumeFalse(System.getProperty("os.name").toLowerCase().startsWith("win"));
        
        String csv = wrapper.runOnFile(new File("test.c"));
        
        String[] lines = csv.split("\n");
        
        assertThat(lines.length, is(6));
        assertThat(lines[0], is("test.c;2;4;if;0;2;CONFIG_A;CONFIG_A"));
        assertThat(lines[1], is("test.c;6;8;if;0;6;(CONFIG_B) || !(CONFIG_C);(CONFIG_B) || !(CONFIG_C)"));
        assertThat(lines[2], is("test.c;8;18;else;0;6;;!((CONFIG_B) || !(CONFIG_C))"));
        assertThat(lines[3], is("test.c;10;12;if;1;10;CONFIG_A;(CONFIG_A) && (!((CONFIG_B) || !(CONFIG_C)))"));
        assertThat(lines[4],
                is("test.c;12;14;elseif;1;10;CONFIG_B;(!(CONFIG_A) && (CONFIG_B)) && (!((CONFIG_B) || !(CONFIG_C)))"));
        assertThat(lines[5],
                is("test.c;14;16;else;1;10;;(!(CONFIG_A) && (!(CONFIG_B))) && (!((CONFIG_B) || !(CONFIG_C)))"));
    }
    
    /**
     * Tests whether undertaker correctly handles missing files.
     * 
     * @throws IOException unwanted.
     */
    @Test
    public void testUndertakerNonExisting() throws IOException {
        Assume.assumeFalse(System.getProperty("os.name").toLowerCase().startsWith("win"));
        
        String csv = wrapper.runOnFile(new File("non_existing.c"));
        assertThat(csv, nullValue());
    }
    
}
