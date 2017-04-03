package de.uni_hildesheim.sse.kernel_haven.undertaker;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import de.uni_hildesheim.sse.kernel_haven.code_model.Block;
import de.uni_hildesheim.sse.kernel_haven.util.FormatException;
import de.uni_hildesheim.sse.kernel_haven.util.logic.Formula;
import de.uni_hildesheim.sse.kernel_haven.util.logic.parser.ExpressionFormatException;
import de.uni_hildesheim.sse.kernel_haven.util.logic.parser.Parser;

/**
 * The Undertaker representation of a Block. A Block are the lines between the
 * <code>#ifdef</code> and <code>#endif</code> or <code>#elif</code> and
 * <code>#endif</code> or <code>#ifdef</code> and <code>#endif</code>
 * 
 * @author Johannes
 * @author Adam
 */
public class UndertakerBlock extends Block {

    private List<Block> children;

    private int lineStart;

    private int lineEnd;

    private Formula condition;

    private Formula presenceCondition;

    /**
     * Creates a new Block.
     * 
     * @param lineStart
     *            The starting line of this block.
     * @param lineEnd
     *            The end line of this block.
     * @param condition
     *            The immediate condition of this block.
     * @param presenceCondition
     *            The pc. Must not be <code>null</code>.
     */
    public UndertakerBlock(int lineStart, int lineEnd, Formula condition, Formula presenceCondition) {
        children = new LinkedList<>();
        this.lineStart = lineStart;
        this.lineEnd = lineEnd;
        this.condition = condition;
        this.presenceCondition = presenceCondition;
    }

    @Override
    public Iterator<Block> iterator() {
        return children.iterator();
    }

    @Override
    public int getNestedBlockCount() {
        return children.size();
    }

    @Override
    public int getLineStart() {
        return lineStart;
    }

    @Override
    public int getLineEnd() {
        return lineEnd;
    }

    @Override
    public Formula getCondition() {
        return condition;
    }

    @Override
    public Formula getPresenceCondition() {
        return presenceCondition;
    }

    @Override
    public void addChild(Block block) {
        this.children.add(block);
    }

    @Override
    public List<String> serializeCsv() {
        List<String> result = new ArrayList<>(4);
        
        result.add(lineStart + "");
        result.add(lineEnd + "");
        result.add(condition == null ? "null" : condition.toString());
        result.add(presenceCondition.toString());
        
        return result;
    }
    
    /**
     * Deserializes the given CSV into a block.
     * 
     * @param csv The csv.
     * @param parser The parser to parse boolean formulas.
     * @return The deserialized block.
     * 
     * @throws FormatException If the CSV is malformed.
     */
    public static UndertakerBlock createFromCsv(String[] csv, Parser<Formula> parser) throws FormatException {
        if (csv.length != 4) {
            throw new FormatException("Invalid CSV");
        }
        
        int lineStart = Integer.parseInt(csv[0]);
        int lineEnd = Integer.parseInt(csv[1]);
        Formula condition = null;
        if (!csv[2].equals("null")) {
            try {
                condition = parser.parse(csv[2]);
            } catch (ExpressionFormatException e) {
                throw new FormatException(e);
            }
        }
        
        Formula presenceCondition;
        try {
            presenceCondition = parser.parse(csv[3]);
        } catch (ExpressionFormatException e) {
            throw new FormatException(e);
        }
        
        return new UndertakerBlock(lineStart, lineEnd, condition, presenceCondition);
    }

}
