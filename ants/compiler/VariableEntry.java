package ants.compiler;

import java.util.*;

/**
 * The <code>VariableEntry</code> class stores information about variables
 * used in the {@link AntsCompiler} class.
 */
public class VariableEntry {

    /**
     * The identifier of the variable.
     */
    public String name;

    /**
     * The number of the slot where this variable is stored.
     */
    public short address;

    /**
     * Creates a new <code>VariableEntry</code>.
     *
     * @param name The identifier of the variable.
     * @param address The number of the slot where this variable is stored.
     */
    public VariableEntry(String name, short address) {
        this.name = name;
        this.address = address;
    }
}
