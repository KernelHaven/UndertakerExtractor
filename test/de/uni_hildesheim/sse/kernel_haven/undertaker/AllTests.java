package de.uni_hildesheim.sse.kernel_haven.undertaker;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * The Class AllTests.
 */
@RunWith(Suite.class)
@SuiteClasses({
    UndertakerWrapperTest.class,
    CsvToAstConverterTest.class,
    })
public class AllTests {
    // runs tests defined in SuiteClasses
}
