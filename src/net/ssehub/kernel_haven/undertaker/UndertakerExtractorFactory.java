package net.ssehub.kernel_haven.undertaker;

import net.ssehub.kernel_haven.SetUpException;
import net.ssehub.kernel_haven.code_model.ICodeExtractorFactory;
import net.ssehub.kernel_haven.code_model.ICodeModelExtractor;
import net.ssehub.kernel_haven.config.CodeExtractorConfiguration;

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
