/*
 * Copyright 2017-2019 University of Hildesheim, Software Systems Engineering
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.ssehub.kernel_haven.undertaker;

import java.io.File;
import java.io.IOException;

import net.ssehub.kernel_haven.SetUpException;
import net.ssehub.kernel_haven.code_model.AbstractCodeModelExtractor;
import net.ssehub.kernel_haven.code_model.CodeBlock;
import net.ssehub.kernel_haven.code_model.SourceFile;
import net.ssehub.kernel_haven.config.Configuration;
import net.ssehub.kernel_haven.config.DefaultSettings;
import net.ssehub.kernel_haven.config.Setting;
import net.ssehub.kernel_haven.config.Setting.Type;
import net.ssehub.kernel_haven.util.CodeExtractorException;
import net.ssehub.kernel_haven.util.ExtractorException;
import net.ssehub.kernel_haven.util.FormatException;
import net.ssehub.kernel_haven.util.Util;

/**
 * Wrapper to run Undertaker to extract code blocks.
 * 
 * @author Adam
 * @author Johannes
 */
public class UndertakerExtractor extends AbstractCodeModelExtractor {

    private static final Setting<Integer> HANG_TIMEOUT
        = new Setting<>("code.extractor.hang_timeout", Type.INTEGER, true, "20000", "Undertaker has a bug where it "
                + "hangs forever on some few files of the Linux Kernel. This setting defines a timeout in milliseconds "
                + "until the undertaker executable is forcibly terminated.");
    
    private File linuxSourceTree;
    
    /**
     * The directory where this extractor can store its resources. Not null.
     */
    private File resourceDir;
    
    private boolean fuzzyBooleanParsing;
    
    private UndertakerWrapper wrapper;
    
    @Override
    protected void init(Configuration config) throws SetUpException {
        linuxSourceTree = config.getValue(DefaultSettings.SOURCE_TREE);

        resourceDir = Util.getExtractorResourceDir(config, getClass());

        
        fuzzyBooleanParsing = config.getValue(DefaultSettings.FUZZY_PARSING);
        
        config.registerSetting(HANG_TIMEOUT);
        long timeout = config.getValue(HANG_TIMEOUT);
        
        try {
            wrapper = new UndertakerWrapper(resourceDir, linuxSourceTree, timeout);
        } catch (IOException e) {
            throw new SetUpException(e);
        }
    }

    @Override
    protected SourceFile<CodeBlock> runOnFile(File target) throws ExtractorException {
        try {
            
            String csv = wrapper.runOnFile(target);
    
            if (csv == null) {
                throw new CodeExtractorException(target, "Undertaker execution not successful");
            }
            CsvToAstConverter converter = new CsvToAstConverter(fuzzyBooleanParsing);
            SourceFile<CodeBlock> result = converter.convert(target, csv);
    
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
