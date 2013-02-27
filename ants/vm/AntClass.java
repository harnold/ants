package ants.vm;

import java.io.*;

/**
 * The <code>AntClass</code> class represents the kind of an ant.  An ant
 * class is compiled from an ant source file, so all ants of a certain kind
 * share the same program, backpack size, and number of variables.
 */
public class AntClass implements Serializable {

    private String name;
    private short id;
    private short backpackSize;
    private short variableSize;
    private short programSize;
    private short[] program;

    private transient int player;

    /**
     * Returns the player that this ant class is assigned to.
     *
     * @return The index of the player of this ant class.
     */
    public int getPlayer() {
        return player;
    }

    /**
     * Assigns this ant class to a player.  Several players can use the same
     * ant class, but must use different <code>AntClass</code> instances.
     *
     * @param value The index of the player.
     */
    public void setPlayer(int value) {
        player = value;
    }

    /**
     * Returns the size of the variable store of this ant class.  This is the
     * number of variables that ants of this kind can use.
     *
     * @return The size of the variable store.
     */
    public short getVariableSize() {
        return variableSize;
    }

    /**
     * Sets the size of the variable store for this ant class.  This is the
     * number of variables that ants of this kind can use.
     *
     * @param value The new size of the variable store.
     */
    public void setVariableSize(short value) {
        variableSize = value;
    }

    /**
     * Returns the size of the program of this ant class.  The size is the
     * number of <code>short</code> values, which equals the number of
     * instructions multiplied by the instruction size (which is 4).
     *
     * @return The program size.
     */
    public short getProgramSize() {
        return programSize;
    }

    /**
     * Sets the size of the program of this ant class.
     *
     * @param value The new program size.
     */
    public void setProgramSize(short value) {
        programSize = value;
    }

    /**
     * Returns the name of this ant class.  This is the identifier given in
     * the <i>DefineAnt</i> declaration in the ant source file.
     *
     * @return The name of the ant class.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the ant class.
     *
     * @param value The new name of the ant class.
     */
    public void setName(String value) {
        name = value;
    }

    /**
     * Returns the numeric id of the ant class.  This is the number given in
     * the <i>DefineAnt</i> declaration in the ant source file.  The id is
     * unique among all ant classes in one simulation.
     *
     * @return The numeric id of the ant class.
     */
    public short getId() {
        return id;
    }

    /**
     * Sets the numeric id of the ant class.  This id must be unique among all
     * ant classes in one simulation.
     *
     * @param value The new numeric id.
     */
    public void setId(short value) {
        id = value;
    }

    /**
     * Returns the backpack size of the ant class.  This is the maximum number
     * of items that ants of this kind can carry.  This value is given in the
     * <i>$MyBackpackSize</i> declaration in the ant source file.
     *
     * @return The backpack size.
     */
    public short getBackpackSize() {
        return backpackSize;
    }

    /**
     * Sets the backpack size of this ant class.  This is the maximum number
     * of items that ants of this kind can carry.
     *
     * @param value The new backpack size.
     */
    public void setBackpackSize(short value) {
        backpackSize = value;
    }

    /**
     * Returns the program (the instruction stream) of this ant class.
     *
     * @return The program of the ant class.
     */
    public short[] getProgram() {
        return program;
    }

    /**
     * Sets the program (the instruction stream) for this ant class.
     *
     * @param value The new program for this ant class.
     */
    public void setProgram(short[] value) {
        program = value;
    }
}
