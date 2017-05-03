package de.uni_hildesheim.sse.kernel_haven.undertaker;

import java.io.File;
import java.util.Stack;

import de.uni_hildesheim.sse.kernel_haven.code_model.SourceFile;
import de.uni_hildesheim.sse.kernel_haven.util.FormatException;
import de.uni_hildesheim.sse.kernel_haven.util.logic.Formula;
import de.uni_hildesheim.sse.kernel_haven.util.logic.parser.CStyleBooleanGrammar;
import de.uni_hildesheim.sse.kernel_haven.util.logic.parser.ExpressionFormatException;
import de.uni_hildesheim.sse.kernel_haven.util.logic.parser.Parser;
import de.uni_hildesheim.sse.kernel_haven.util.logic.parser.VariableCache;

/**
 * Converts the pilztaker output to AST ({@link Block}s).
 * 
 * @author Adam
 * @author Johannes
 *
 */
public class CsvToAstConverter {

    private static final String DELIMITER = ";";

    private VariableCache cache = new VariableCache();
    
    private Parser<Formula> parser = new Parser<>(new CStyleBooleanGrammar(cache));
    
    private boolean fuzzyParsing;
    
    /**
     * Creates a converter, that reads CSV output from pilztaker and converts it into {@link SourceFile}s.
     * 
     * @param fuzzyParsing Whether parsing of boolean formulas should be strict or fuzzy.
     */
    public CsvToAstConverter(boolean fuzzyParsing) {
        this.fuzzyParsing = fuzzyParsing;
    }
    
    /**
     * Tries to fuzzy parse the boolean formula. This should be used only if normal parsing was
     * not successful.
     * 
     * @param formula The string to parse.
     * @return The resulting parsed formula. Never <code>null</code>.
     * 
     * @throws ExpressionFormatException If the string still cannot be parsed.
     */
    private Formula fuzzyParse(String formula) throws ExpressionFormatException {
        
        formula = formula.replaceAll("\\s*(!=|[<>=]{1,2})\\s*", "_");
        
        Formula result = null;
        try {
            result = parser.parse(formula);
        } finally {
            cache.clear();
        }
        
        return result;
    }
    
    /**
     * Parses the given string into a formula. If fuzzyParsing is true, then this
     * tries some heuristics to parse strings that otherwise wouldn't be parseable. 
     * 
     * @param formula The string to parse.
     * @return The parsed formula. Never <code>null</code>.
     * 
     * @throws ExpressionFormatException If the string cannot be parsed.
     */
    private Formula tryParse(String formula) throws ExpressionFormatException {
        Formula result = null;
        try {
            result = parser.parse(formula);
            cache.clear();
            
        } catch (ExpressionFormatException e) {
            cache.clear();
            if (!fuzzyParsing) {
                throw e;
            }
            try {
                result = fuzzyParse(formula);
            } catch (ExpressionFormatException e2) {
                throw e;
            }
        }
        return result;
    }
    
    /**
     * Converts the given CSV into a {@link SourceFile}.
     * 
     * @param filePath The path of the source file that was parsed. Relative to source tree.
     * @param csv The CSV output of pilztaker.
     * @return The {@link SourceFile} representing the CSV content. Never <code>null</code>.
     * 
     * @throws FormatException If the format of the CSV is not valid.
     */
    public SourceFile convert(File filePath, String csv) throws FormatException {
        SourceFile result = new SourceFile(filePath);
        
        Stack<UndertakerBlock> stack = new Stack<>();
        
        try {
            for (String line : csv.split("\n")) {
                if (line.trim().isEmpty()) {
                    continue;
                }
                String[] parts = line.split(DELIMITER);
                
                if (parts.length != 8) {
                    throw new FormatException("Invalid CSV line in file " + filePath.getPath() + ": " + line );
                }
                
                File filename = new File(parts[0]);
                if (!filename.equals(filePath)) {
                    throw new FormatException("Found invalid file in CSV: " + parts[0] + "; expected "
                            + filePath.getPath());
                }
                
                int lineStart = Integer.parseInt(parts[1]);
                int lineEnd = Integer.parseInt(parts[2]);
                String type = parts[3];
                
                if (!type.equals("if") && !type.equals("elseif") && !type.equals("else") && !type.equals("ifndef")) {
                    throw new FormatException("Unexpected type found: " + type);
                }
                
                int nestingDepth = Integer.parseInt(parts[4]);
//                int corespondingIf = Integer.parseInt(parts[5]);
                
                Formula condition = null;
                if (!parts[6].isEmpty()) {
                    condition = tryParse(parts[6]);
                }
                
                Formula pc = tryParse(parts[7]);

                while (stack.size() > nestingDepth) {
                    stack.pop();
                }
                
                UndertakerBlock block = new UndertakerBlock(lineStart, lineEnd, condition, pc);
                if (nestingDepth == 0) {
                    result.addBlock(block);
                } else {
                    stack.peek().addChild(block);
                }
                
                stack.push(block);
            }
        
        } catch (NumberFormatException e) {
            throw new FormatException(e);
        } catch (ExpressionFormatException e) {
            throw new FormatException(e);
        }
        return result;
    }
    
}
