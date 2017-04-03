package de.uni_hildesheim.sse.kernel_haven.undertaker;

import de.uni_hildesheim.sse.kernel_haven.SetUpException;
import de.uni_hildesheim.sse.kernel_haven.code_model.ICodeExtractorFactory;
import de.uni_hildesheim.sse.kernel_haven.code_model.ICodeModelExtractor;
import de.uni_hildesheim.sse.kernel_haven.config.CodeExtractorConfiguration;

/**
 * Factory for the undertaker extractor.
 * 
 * @author Alice
 * @author Adam
 */
public class UndertakerExtractorFactory implements ICodeExtractorFactory {

    @Override
    public ICodeModelExtractor create(CodeExtractorConfiguration config) throws SetUpException {
        return new UndertakerExtractor(config);
    }

}
