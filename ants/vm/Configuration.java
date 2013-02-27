package ants.vm;

import java.util.*;

/**
 * The <code>Configuration</code> class stores the configuration data for an
 * {@link AntsVm} instance.
 */
public class Configuration {

    /**
     * The <code>PlayerInfo</code> class stores the configuration data for a
     * single player.
     */
    public static class PlayerInfo {

        /**
         * The name of the player.
         */
        public String name;

        /**
         * A list of the ant class file names of the player.  Every element of
         * this list must be a <code>String</code> containing the path to a
         * binary ant file.
         */
        public List<String> classFiles = new ArrayList<>();
    }

    /**
     * The path where to find the data files.
     */
    public String dataPath;

    /**
     * An array of {@link PlayerInfo} objects, one for each player.
     */
    public PlayerInfo[] playerInfos;

    /**
     * The number of players.  Currently, a maximum of 4 players is allowed.
     */
    public int numberOfPlayers;

    /**
     * The width of the playfield in cells.
     */
    public int playfieldWidth = 1000;

    /**
     * The height of the playfield in cells.
     */
    public int playfieldHeight = 1000;

    /**
     * The ratio of passable cells in the playfield.  A ratio of <i>x</i>
     * means that about (100 * <i>x</i>) percent of the cells will be
     * passable.
     */
    public double passableRatio = 0.9;

    /**
     * Twice the ratio of cells that contain food.  A ratio of <i>x</i> means
     * that about (100 * <i>x</i>/2) percent of the passable cells will
     * contain food.
     */
    public double foodRatio = 0.3;

    /**
     * Twice the ratio of cells that contain stones.  A ratio of <i>x</i>
     * means that about (100 * <i>x</i>/2) percent of the passable cells will
     * contain stones.
     */
    public double stonesRatio = 0.3;

    /**
     * The maximum number of stones that is initially placed on a single cell.
     */
    public short maxFoodPerCell = 20;

    /**
     * The maximum number of food that is initially placed on a single cell.
     */
    public short maxStonesPerCell = 20;

    /**
     * The number of milliseconds to sleep after the execution of a cycle.
     */
    public long sleepPerCycle = 0;

    /**
     * The initial energy an ant has when it is born.
     */
    public short initialEnergy = 10000;

    /**
     * The number of energy units an ant can use in a single cycle.
     */
    public short energyPerRun = 20;

    /**
     * The number of energy units an ant gets by consuming one food unit.
     */
    public short energyPerFood = 1000;

    /**
     * The rate of food regrowth.
     */
    public double foodRegrowRate = 0.001;
}
