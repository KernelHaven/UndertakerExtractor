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
        
        VariableCache cache = new VariableCache();
        Parser<Formula> parser = new Parser<>(new CStyleBooleanGrammar(cache));
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
                    condition = parser.parse(parts[6]);
                    cache.clear();
                }
                
                Formula pc = parser.parse(parts[7]);
                cache.clear();

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
