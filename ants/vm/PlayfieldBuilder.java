package ants.vm;

import java.util.*;

/**
 * The <code>PlayfieldBuilder</code> class provides a simple factory for Ants
 * playfields.  It creates a playfield with a given dimension and randomly
 * places stones and food at the cells.
 */
public class PlayfieldBuilder {

    /**
     * Twice the ratio of cells that contain stones.  A ratio of <i>x</i>
     * means that about (100 * <i>x</i>/2) percent of the passable cells will
     * contain stones.
     */
    public double stonesRatio = 0.3;

    /**
     * Twice the ratio of cells that contain food.  A ratio of <i>x</i> means
     * that about (100 * <i>x</i>/2) percent of the passable cells will
     * contain food.
     */
    public double foodRatio = 0.3;

    /**
     * The ratio of passable cells in the playfield.  A ratio of <i>x</i>
     * means that about (100 * <i>x</i>) percent of the cells will be
     * passable.
     */
    public double passableRatio = 0.9;

    /**
     * The maximum number of stones that is initially placed on a single cell.
     */
    public int maxStonesPerCell = 20;

    /**
     * The maximum number of food that is initially placed on a single cell.
     */
    public int maxFoodPerCell = 20;

    /**
     * Creates a new playfield using the parameters set.
     *
     * @param numberOfPlayers The number of players.
     * @param playfieldWidth The number of cells in horizontal direction.
     * @param playfieldHeight The number of cells in vertical direction.
     * @return A matrix of playfield cells.
     */
    public PlayfieldCell[][] createPlayfield(
            int numberOfPlayers, int playfieldWidth, int playfieldHeight) {

        Random random = new Random();

        PlayfieldCell[][] playfield =
            new PlayfieldCell[playfieldHeight][playfieldWidth];

        for (int i = 0; i < playfield.length; i++) {
            for (int j = 0; j < playfield[i].length; j++) {

                PlayfieldCell cell = new PlayfieldCell(numberOfPlayers);

                if (random.nextDouble() < passableRatio) {

                    cell.isPassable = true;

                    if (random.nextBoolean() == true) {
                        if (random.nextDouble() < stonesRatio)
                            cell.stones = (short) random.nextInt(maxStonesPerCell);
                    } else {
                        if (random.nextDouble() < foodRatio)
                            cell.food = (short) random.nextInt(maxFoodPerCell);
                    }
                } else {
                    cell.isPassable = false;
                }

                playfield[i][j] = cell;
            }
        }

        return playfield;
    }
}
