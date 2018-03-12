package net.ssehub.kernel_haven.undertaker;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.IOException;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import net.ssehub.kernel_haven.RunOnlyOnLinux;

/**
 * Tests the undertaker wrapper.
 * 
 * @author Adam
 * @author Johannes
 */
@RunWith(value = RunOnlyOnLinux.class)
public class UndertakerWrapperTest {

    private static final File RESOURCE_DIR = new File("testdata/tmp_res");
    
    private static final File SOURCE_DIR = new File("testdata");
    
    private UndertakerWrapper wrapper;
    
    /**
     * Clears the temporary resource directory.
     */
    @AfterClass
    public static void afterClass() {
        for (File file : RESOURCE_DIR.listFiles()) {
            if (!file.getName().equals(".gitignore")) {
                file.delete();
            }
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
        String csv = wrapper.runOnFile(new File("test.c"));
        
        String[] lines = csv.split("\n");
        
        assertThat(lines.length, is(6));
        assertThat(lines[0], is("test.c;2;4;if;0;2;CONFIG_A;CONFIG_A"));
        assertThat(lines[1], is("test.c;6;8;if;0;6;(CONFIG_B) || !(CONFIG_C);(CONFIG_B) || !(CONFIG_C)"));
        assertThat(lines[2], is("test.c;8;18;else;0;6;!((CONFIG_B) || !(CONFIG_C));!((CONFIG_B) || !(CONFIG_C))"));
        assertThat(lines[3], is("test.c;10;12;if;1;10;CONFIG_A;(CONFIG_A) && (!((CONFIG_B) || !(CONFIG_C)))"));
        assertThat(lines[4],
                is("test.c;12;14;elseif;1;10;!(CONFIG_A) && (CONFIG_B);"
                        + "(!(CONFIG_A) && (CONFIG_B)) && (!((CONFIG_B) || !(CONFIG_C)))"));
        assertThat(lines[5],
                is("test.c;14;16;else;1;10;!(CONFIG_A) && (!(CONFIG_B));"
                        + "(!(CONFIG_A) && (!(CONFIG_B))) && (!((CONFIG_B) || !(CONFIG_C)))"));
    }
    
    /**
     * Tests whether windows newlines are handled correctly.
     * 
     * @throws IOException unwanted. 
     */
    @Test
    public void testWindowsNewlines() throws IOException {
        String csv = wrapper.runOnFile(new File("test_newline.c"));
        
        String[] lines = csv.split("\n");
        
        assertThat(lines.length, is(6));
        assertThat(lines[0], is("test_newline.c;2;4;if;0;2;CONFIG_A;CONFIG_A"));
        assertThat(lines[1], is("test_newline.c;6;9;if;0;6;(CONFIG_B) || !(CONFIG_C);(CONFIG_B) || !(CONFIG_C)"));
        assertThat(lines[2], is("test_newline.c;9;19;else;0;6;!((CONFIG_B) || !(CONFIG_C));"
                + "!((CONFIG_B) || !(CONFIG_C))"));
        assertThat(lines[3], is("test_newline.c;11;13;if;1;11;CONFIG_A;(CONFIG_A) && (!((CONFIG_B) || !(CONFIG_C)))"));
        assertThat(lines[4],
                is("test_newline.c;13;15;elseif;1;11;!(CONFIG_A) && (CONFIG_B);(!(CONFIG_A) && (CONFIG_B)) && "
                        + "(!((CONFIG_B) || !(CONFIG_C)))"));
        assertThat(lines[5],
                is("test_newline.c;15;17;else;1;11;!(CONFIG_A) && (!(CONFIG_B));"
                        + "(!(CONFIG_A) && (!(CONFIG_B))) && (!((CONFIG_B) || !(CONFIG_C)))"));
    }
    
    /**
     * Tests whether undertaker correctly handles missing files.
     * 
     * @throws IOException unwanted.
     */
    @Test
    public void testUndertakerNonExisting() throws IOException {
        String csv = wrapper.runOnFile(new File("non_existing.c"));
        assertThat(csv, nullValue());
    }
    
}
