package ants.compiler;

/**
 * A <code>BackpatchInfo</code> object stores the place where a label address
 * has to be backpatched after compilation.
 */
public class BackpatchInfo {

    /**
     * The number of the instruction.
     */
    public int instruction;

    /**
     * The byte offset from the start of the instruction.
     */
    public int offset;

    /**
     * Creates a new <code>BackpatchInfo</code> object.
     */
    public BackpatchInfo(int instruction, int offset) {
        this.instruction = instruction;
        this.offset = offset;
    }
}
