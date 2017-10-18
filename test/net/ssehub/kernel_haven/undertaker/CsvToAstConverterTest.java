package net.ssehub.kernel_haven.undertaker;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.util.Iterator;

import org.junit.Test;

import net.ssehub.kernel_haven.code_model.CodeElement;
import net.ssehub.kernel_haven.code_model.SourceFile;
import net.ssehub.kernel_haven.util.FormatException;
import net.ssehub.kernel_haven.util.logic.Conjunction;
import net.ssehub.kernel_haven.util.logic.Formula;
import net.ssehub.kernel_haven.util.logic.Negation;
import net.ssehub.kernel_haven.util.logic.Variable;

/**
 * Tests the converter.
 * 
 * @author Adam
 * @author Johannes
 */
public class CsvToAstConverterTest {

    /**
     * Tests whether simple blocks are correctly converted.
     * 
     * @throws FormatException unwanted.
     */
    @Test
    public void testSimple() throws FormatException {
        String csv = "test.c;2;4;if;0;2;CONFIG_A;CONFIG_A\n"
                + "test.c;6;12;if;0;6;CONFIG_B && !CONFIG_C;CONFIG_B && !CONFIG_C";
        
        CsvToAstConverter converter = new CsvToAstConverter(false);
        SourceFile result = converter.convert(new File("test.c"), csv);
        
        assertThat(result.getPath(), is(new File("test.c")));
        assertThat(result.getTopElementCount(), is(2));
        
        Iterator<CodeElement> it = result.iterator();
        
        CodeElement block = it.next();
        assertThat(block.getLineStart(), is(2));
        assertThat(block.getLineEnd(), is(4));
        assertThat(block.getNestedElementCount(), is(0));
        assertThat(block.getCondition(), is(new Variable("CONFIG_A")));
        assertThat(block.getPresenceCondition(), is(new Variable("CONFIG_A")));
        
        block = it.next();
        assertThat(block.getLineStart(), is(6));
        assertThat(block.getLineEnd(), is(12));
        assertThat(block.getNestedElementCount(), is(0));
        Formula condition = new Conjunction(new Variable("CONFIG_B"), new Negation(new Variable("CONFIG_C")));
        assertThat(block.getCondition(), is(condition));
        assertThat(block.getPresenceCondition(), is(condition));
        
        assertThat(it.hasNext(), is(false));
    }
    
    /**
     * Tests whether simple blocks are correctly converted.
     * 
     * The structure that is tested:
     * line 1 CONFIG_A
     *     line 2 CONFIG_B
     *     line 3
     *     
     *     line 4 CONFIG_C
     *         line 5 CONFIG_D
     *         line 6
     *     
     *     line 50
     *     
     *     line 51 CONFIG_E
     *     line 52
     * 
     * line 100
     *
     * 
     * @throws FormatException unwanted.
     */
    @Test
    public void testNesting() throws FormatException {
        String csv = "test.c;1;100;if;0;1;CONFIG_A;CONFIG_A\n"
                + "test.c;2;3;if;1;2;CONFIG_B;(CONFIG_B) && (CONFIG_A)\n"
                + "test.c;4;50;if;1;4;CONFIG_C;(CONFIG_C) && (CONFIG_A)\n"
                + "test.c;5;6;if;2;5;CONFIG_D;CONFIG_D && ((CONFIG_C) && (CONFIG_A))\n"
                + "test.c;51;52;if;1;51;CONFIG_E;CONFIG_E && CONFIG_A\n";
        
        CsvToAstConverter converter = new CsvToAstConverter(false);
        SourceFile result = converter.convert(new File("test.c"), csv);
        
        assertThat(result.getPath(), is(new File("test.c")));
        assertThat(result.getTopElementCount(), is(1));
        
        Iterator<CodeElement> it = result.iterator();
        
        CodeElement block = it.next();
        assertThat(block.getLineStart(), is(1));
        assertThat(block.getLineEnd(), is(100));
        assertThat(block.getNestedElementCount(), is(3));
        assertThat(block.getCondition(), is(new Variable("CONFIG_A")));
        assertThat(block.getPresenceCondition(), is(new Variable("CONFIG_A")));
        
        assertThat(it.hasNext(), is(false));
        
        it = block.iterateNestedElements().iterator();
        
        CodeElement nested = it.next();
        assertThat(nested.getLineStart(), is(2));
        assertThat(nested.getLineEnd(), is(3));
        assertThat(nested.getNestedElementCount(), is(0));
        Formula condition = new Conjunction(new Variable("CONFIG_B"), new Variable("CONFIG_A"));
        assertThat(nested.getCondition(), is(new Variable("CONFIG_B")));
        assertThat(nested.getPresenceCondition(), is(condition));
        
        nested = it.next();
        assertThat(nested.getLineStart(), is(4));
        assertThat(nested.getLineEnd(), is(50));
        assertThat(nested.getNestedElementCount(), is(1));
        condition = new Conjunction(new Variable("CONFIG_C"), new Variable("CONFIG_A"));
        assertThat(nested.getCondition(), is(new Variable("CONFIG_C")));
        assertThat(nested.getPresenceCondition(), is(condition));
        
        CodeElement nestedNested = nested.iterateNestedElements().iterator().next();
        assertThat(nestedNested.getLineStart(), is(5));
        assertThat(nestedNested.getLineEnd(), is(6));
        assertThat(nestedNested.getNestedElementCount(), is(0));
        condition = new Conjunction(new Variable("CONFIG_D"),
                new Conjunction(new Variable("CONFIG_C"), new Variable("CONFIG_A")));
        assertThat(nestedNested.getCondition(), is(new Variable("CONFIG_D")));
        assertThat(nestedNested.getPresenceCondition(), is(condition));
        
        nested = it.next();
        assertThat(nested.getLineStart(), is(51));
        assertThat(nested.getLineEnd(), is(52));
        assertThat(nested.getNestedElementCount(), is(0));
        condition = new Conjunction(new Variable("CONFIG_E"), new Variable("CONFIG_A"));
        assertThat(nested.getCondition(), is(new Variable("CONFIG_E")));
        assertThat(nested.getPresenceCondition(), is(condition));

        assertThat(it.hasNext(), is(false));
    }
    
    /**
     * Tests whether else / elseif is correctly parsed.
     * 
     * @throws FormatException unwanted.
     */
    @Test
    public void testElsIfs() throws FormatException {
        String csv = "test.c;1;2;if;0;1;CONFIG_A;CONFIG_A\n"
                + "test.c;2;3;elseif;0;1;!(CONFIG_A) && (CONFIG_B);!(CONFIG_A) && (CONFIG_B)\n"
                + "test.c;4;5;else;0;1;!(CONFIG_A) && (!(CONFIG_B));!(CONFIG_A) && (!(CONFIG_B))\n";
        
        CsvToAstConverter converter = new CsvToAstConverter(false);
        SourceFile result = converter.convert(new File("test.c"), csv);
        
        assertThat(result.getPath(), is(new File("test.c")));
        assertThat(result.getTopElementCount(), is(3));
        
        Iterator<CodeElement> it = result.iterator();
        
        CodeElement block = it.next();
        assertThat(block.getLineStart(), is(1));
        assertThat(block.getLineEnd(), is(2));
        assertThat(block.getNestedElementCount(), is(0));
        assertThat(block.getCondition(), is(new Variable("CONFIG_A")));
        assertThat(block.getPresenceCondition(), is(new Variable("CONFIG_A")));
        
        block = it.next();
        assertThat(block.getLineStart(), is(2));
        assertThat(block.getLineEnd(), is(3));
        assertThat(block.getNestedElementCount(), is(0));
        Formula f = new Conjunction(new Negation(new Variable("CONFIG_A")), new Variable("CONFIG_B"));
        assertThat(block.getCondition(), is(f));
        assertThat(block.getPresenceCondition(), is(f));
        
        block = it.next();
        assertThat(block.getLineStart(), is(4));
        assertThat(block.getLineEnd(), is(5));
        assertThat(block.getNestedElementCount(), is(0));
        f = new Conjunction(new Negation(new Variable("CONFIG_A")), new Negation(new Variable("CONFIG_B")));
        assertThat(block.getCondition(), is(f));
        assertThat(block.getPresenceCondition(), is(f));

        assertThat(it.hasNext(), is(false));
    }
    
    /**
     * Tests whether invalid CSV is detected.
     * @throws FormatException expected.
     */
    @Test(expected = FormatException.class)
    public void testInvalidCsv() throws FormatException {
        String csv = "this isn;t csv";
        CsvToAstConverter converter = new CsvToAstConverter(false);
        converter.convert(new File("test.c"), csv);
    }
    
    /**
     * Tests whether invalid filenames are detected.
     * @throws FormatException expected.
     */
    @Test(expected = FormatException.class)
    public void testInvalidFilename() throws FormatException {
        String csv = "not_test.c;1;2;if;0;1;CONFIG_A;CONFIG_A\n";
        CsvToAstConverter converter = new CsvToAstConverter(false);
        converter.convert(new File("test.c"), csv);
    }
    
    /**
     * Tests whether invalid numbers are detected.
     * @throws FormatException expected.
     */
    @Test(expected = FormatException.class)
    public void testInvalidNumber() throws FormatException {
        String csv = "test.c;1;not_a_number;if;0;1;CONFIG_A;CONFIG_A\n";
        CsvToAstConverter converter = new CsvToAstConverter(false);
        converter.convert(new File("test.c"), csv);
    }
    
    /**
     * Tests whether invalid types are detected.
     * @throws FormatException expected.
     */
    @Test(expected = FormatException.class)
    public void testInvalidType() throws FormatException {
        String csv = "test.c;1;5;wtf;0;1;CONFIG_A;CONFIG_A\n";
        CsvToAstConverter converter = new CsvToAstConverter(false);
        converter.convert(new File("test.c"), csv);
    }
    
    /**
     * Tests whether invalid boolean formulas are detected.
     * @throws FormatException expected.
     */
    @Test(expected = FormatException.class)
    public void testInvalidPc() throws FormatException {
        String csv = "test.c;1;5;if;0;1;CONFIG_A;NOT_A_BOOL ||\n";
        CsvToAstConverter converter = new CsvToAstConverter(false);
        converter.convert(new File("test.c"), csv);
    }
    
    /**
     * Tests whether fuzzy parsing works correctly.
     * 
     * @throws FormatException unwanted.
     */
    @Test
    public void testFuzzyParsing() throws FormatException {
        String csv = "test.c;1;2;if;0;1;__STDC_VERSION__ >= 201112L;A";
        
        CsvToAstConverter converter = new CsvToAstConverter(true);
        SourceFile result = converter.convert(new File("test.c"), csv);
        
        assertThat(result.getPath(), is(new File("test.c")));
        assertThat(result.getTopElementCount(), is(1));
        
        Iterator<CodeElement> it = result.iterator();
        
        CodeElement block = it.next();
        assertThat(block.getLineStart(), is(1));
        assertThat(block.getLineEnd(), is(2));
        assertThat(block.getNestedElementCount(), is(0));
        assertThat(block.getCondition(), is(new Variable("__STDC_VERSION___ge_201112L")));
        assertThat(block.getPresenceCondition(), is(new Variable("A")));

        assertThat(it.hasNext(), is(false));
    }
    
}
