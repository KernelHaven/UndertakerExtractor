package net.ssehub.kernel_haven.undertaker;

import java.io.File;
import java.io.IOException;

import net.ssehub.kernel_haven.SetUpException;
import net.ssehub.kernel_haven.code_model.AbstractCodeModelExtractor;
import net.ssehub.kernel_haven.code_model.SourceFile;
import net.ssehub.kernel_haven.config.CodeExtractorConfiguration;
import net.ssehub.kernel_haven.util.CodeExtractorException;
import net.ssehub.kernel_haven.util.ExtractorException;
import net.ssehub.kernel_haven.util.FormatException;

/**
 * Wrapper to run Undertaker to extract code blocks.
 * 
 * @author Adam
 * @author Johannes
 */
public class UndertakerExtractor extends AbstractCodeModelExtractor {

    private File linuxSourceTree;
    
    /**
     * The directory where this extractor can store its resources. Not null.
     */
    private File resourceDir;
    
    private boolean fuzzyBooleanParsing;
    
    private UndertakerWrapper wrapper;
    
    @Override
    protected void init(CodeExtractorConfiguration config) throws SetUpException {
        linuxSourceTree = config.getSourceTree();
        if (linuxSourceTree == null) {
            throw new SetUpException("Config does not contain source_tree setting");
        }

        resourceDir = config.getExtractorResourceDir(getClass());

        fuzzyBooleanParsing = Boolean.parseBoolean(config.getProperty("code.extractor.fuzzy_parsing", "false"));
        
        long timeout;
        try {
            timeout = Long.parseLong(config.getProperty("code.extractor.hang_timeout", "20000"));
        } catch (NumberFormatException e) {
            throw new SetUpException(e);
        }
        
        try {
            wrapper = new UndertakerWrapper(resourceDir, linuxSourceTree, timeout);
        } catch (IOException e) {
            throw new SetUpException(e);
        }
    }

    @Override
    protected SourceFile runOnFile(File target) throws ExtractorException {
        try {
            
            String csv = wrapper.runOnFile(target);
    
            if (csv == null) {
                throw new CodeExtractorException(target, "Undertaker execution not successful");
            }
            CsvToAstConverter converter = new CsvToAstConverter(fuzzyBooleanParsing);
            SourceFile result = converter.convert(target, csv);
    
            return result;
            
        } catch (IOException | FormatException e) {
            throw new CodeExtractorException(target, e);
        }
    }

    @Override
    protected String getName() {
        return "UndertakerExtractor";
    }

}
