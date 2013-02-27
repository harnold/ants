package ants.vm;

/**
 * The <code>PlayfieldCell</code> class represents a single cell of the
 * playfield.  The playfield is a rectangular matrix of such cells.  A cell
 * can be passable or unpassable.  A passable cell can contain either an ant,
 * or a number of stones, or a number of food items.
 */
public class PlayfieldCell {

    /**
     * The ant that stands on this cell, or <code>null</code> if no ant is
     * here.
     */
    public Ant ant;

    /**
     * If this cell is passable by ants. If this is false, no ant can ever
     * stand on this cell.
     */
    public boolean isPassable;

    /**
     * The number of stones on this cell.
     */
    public short stones;

    /**
     * The number of food items on this cell.
     */
    public short food;

    /**
     * An array of mark values.  There can be a different mark for each player;
     * marks are not shared between tribes.
     */
    public short[] marks;

    /**
     * Creates a new playfield cell.
     *
     * @param numberOfPlayers The number of players in the game.
     */
    public PlayfieldCell(int numberOfPlayers) {
        marks = new short[numberOfPlayers];
    }

    /**
     * Tests if this cell is empty.  A cell is empty if it is passable and
     * there are no ants, no food, and no stones on it.
     *
     * @return True if this cell is empty, false otherwise.
     */
    public boolean isEmpty() {
        return stones == 0 && food == 0 && ant == null && isPassable;
    }
}
