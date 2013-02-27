package ants.compiler;

import java.util.*;

/**
 * A <code>LabelEntry</code> object stores information about a label used in
 * the {@link AntsCompiler} class.
 */
public class LabelEntry {

    /**
     * The identifier of the label.
     */
    public String name;

    /**
     * The address that this label represents.
     */
    public short address;

    /**
     * Whether the label has already been defined.  If the address of this
     * label is known, this value is true.  If it has been used but has not
     * yet been defined (in case of a forward jump), this value is false.
     */
    public boolean defined;

    /**
     * A list of {@link BackpatchInfo} objects that stores all places where
     * the address of this label has to be backpatched.
     */
    public List<BackpatchInfo> backpatchInfos;

    /**
     * Creates a new <code>LabelEntry</code> object.
     *
     * @param name The identifier of the label.
     * @param address The address that this label represents.
     * @param defined If the label has already been defined.
     */
    public LabelEntry(String name, short address, boolean defined) {
        this.name = name;
        this.address = address;
        this.defined = defined;
        this.backpatchInfos = new ArrayList<BackpatchInfo>();
    }
}
