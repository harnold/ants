package ants.vm;

/**
 * The <code>Ant</code> class stores the data associated with a single ant.
 * Every ant is of a certain kind, called its {@link AntClass}.
 * <code>Ant</code> objects are created during the execution of the {@link
 * AntsVm} using the <i>MakeAnt</i> instruction, which can only be executed by
 * ants of a queen class (the first ant class of each player).
 */
public class Ant {

    /**
     * The index of the variable <i>$MyBackpackSize</i>.
     */
    public static final int MY_BACKPACK_SIZE = 0;

    /**
     * The index of the variable <i>$MyFood</i>.
     */
    public static final int MY_FOOD = 1;

    /**
     * The index of the variable <i>$MyStones</i>.
     */
    public static final int MY_STONES = 2;

    /**
     * The index of the variable <i>$MyEnergy</i>.
     */
    public static final int MY_ENERGY = 3;

    /**
     * The index of the variable <i>$MyTribe</i>.
     */
    public static final int MY_TRIBE = 4;

    private int pc;
    private int xpos;
    private int ypos;
    private short[] variables;
    private AntClass antClass;

    /**
     * Returns the horizontal position of this ant.
     *
     * @return The ant's horizontal position.
     */
    public int getXPos() {
        return xpos;
    }

    /**
     * Sets the horizontal position of this ant.
     *
     * @param value The ant's new horizontal position.
     */
    public void setXPos(int value) {
        xpos = value;
    }

    /**
     * Returns the vertical position of this ant.
     *
     * @return The ant's vertical position.
     */
    public int getYPos() {
        return ypos;
    }

    /**
     * Sets the vertical position of this ant.
     *
     * @param value The ant's new vertical position.
     */
    public void setYPos(int value) {
        ypos = value;
    }

    /**
     * Sets the position of this ant.
     *
     * @param x The ant's new horizontal position.
     * @param y The ant's new vertical position.
     */
    public void setPos(int x, int y) {
        xpos = x;
        ypos = y;
    }

    /**
     * Returns the variable store of this ant.
     *
     * @return An array containing the ant's variable values.
     */
    public short[] getVariables() {
        return variables;
    }

    /**
     * Returns a variable value of this ant.
     *
     * @param index The variable index.
     * @return The value of the variable with the given index.
     */
    public short getVariable(int index) {
        return variables[index];
    }

    /**
     * Sets a variable value of this ant.
     *
     * @param index The variable index.
     * @param value The new variable value.
     */
    public void setVariable(int index, short value) {
        variables[index] = value;
    }

    /**
     * Returns the value of the program counter of this ant.
     *
     * @return The value of the ant's program counter.
     */
    public int getPC() {
        return pc;
    }

    /**
     * Sets the program counter of this ant.
     *
     * @param value The new value of the ant's program counter.
     */
    public void setPC(int value) {
        pc = value;
    }

    /**
     * Returns the amount of food that this ant carries.
     *
     * @return The number of food items.
     */
    public short getFood() {
        return variables[MY_FOOD];
    }

    /**
     * Sets the amount of food that this ant carries.
     *
     * @param value The new number of food items.
     */
    public void setFood(short value) {
        variables[MY_FOOD] = value;
    }

    /**
     * Returns the number of stones that this ant carries.
     *
     * @return The number of stones.
     */
    public short getStones() {
        return variables[MY_STONES];
    }

    /**
     * Sets the number of stones that this ant carries.
     *
     * @param value The new number stones.
     */
    public void setStones(short value) {
        variables[MY_STONES] = value;
    }

    /**
     * Returns the amount of energy left for this ant.
     *
     * @return The number of energy units left.
     */
    public short getEnergy() {
        return variables[MY_ENERGY];
    }

    /**
     * Sets the amount of energy of this ant.
     *
     * @param value The new number of energy units left.
     */
    public void setEnergy(short value) {
        variables[MY_ENERGY] = value;
    }

    /**
     * Returns the tribe this ant belongs to.
     *
     * @return The tribe index.
     */
    public short getTribe() {
        return variables[MY_TRIBE];
    }

    /**
     * Returns the class (kind) of this ant.
     *
     * @return The ant's class, represented by an {@link AntClass} object.
     */
    public AntClass getAntClass() {
        return antClass;
    }

    /**
     * Creates a new <code>Ant</code> object.
     *
     * @param antClass The class (kind) of the new ant.
     * @param xpos The horizontal position where the ant is to be created.
     * @param ypos The vertical position where the ant is to be created.
     * @param tribe The index of the tribe the new ant belongs to.
     */
    public Ant(AntClass antClass, int xpos, int ypos, short tribe,
               short initialEnergy) {

        this.antClass = antClass;
        this.variables = new short[antClass.getVariableSize()];
        this.xpos = xpos;
        this.ypos = ypos;

        variables[MY_BACKPACK_SIZE] = antClass.getBackpackSize();
        variables[MY_FOOD]   = 0;
        variables[MY_STONES] = 0;
        variables[MY_ENERGY] = initialEnergy;
        variables[MY_TRIBE]  = tribe;
    }

    /**
     * Advances the program counter by one instruction.
     */
    public void nextInstruction() {
        pc = pc + AntsVm.INSTRUCTION_SIZE;
    }

    /**
     * Returns the the backpack space of the ant.  This is the number of
     * stones or food items that can still be put on the ant, which equals
     * <i>$MyBackpackSize</i> - <i>$MyFood</i> - <i>$MyStones</i>.
     *
     * @return The number of free slots in the ant's backpack.
     */
    public int getBackpackSpace() {
        return antClass.getBackpackSize() - getFood() - getStones();
    }
}
