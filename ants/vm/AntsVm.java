package ants.vm;

import java.io.*;
import java.util.*;
import java.lang.reflect.*;

/**
 * The <code>AntsVm</code> class provides the virtual machine of the Ants
 * system.  The virtual machine runs a separate thread, so that the {@link
 * AntsServer} that provides the connection to the outer world is not blocked.
 */
public class AntsVm extends Thread {

    /**
     * The possible states of an {@link AntsVm} virtual machine.
     */
    public static enum VmState {
        /**
         * The VM has been created but not yet started.
         */
        CREATED,
        /**
         * The VM is running and processing instructions.
         */
        RUNNING,
        /**
         * The VM has been temporarily halted by the {@link #suspendVm()}
         * method.
         */
        SUSPENDED,
        /**
         * The VM has been stopped by the {@link #stopVm()} method.
         */
        STOPPED_BY_COMMAND,
        /**
         * The VM has been stopped because the simulation has ended.
         */
        STOPPED_BY_SIM,
        /**
         * The VM thread has been terminated by another thread.
         */
        TERMINATED
    };

    /**
     * The number of available instructions.
     */
    public static final int NUM_INSTRUCTIONS = 36;

    /**
     * The index of the first variable slot that can be written by an ant.
     */
    public static final int FIRST_USER_VAR = 5;

    /**
     * The maximum program size in number of instructions.
     */
    public static final int MAX_PROGRAM_SIZE = 32000;

    /**
     * The length of an instruction in shorts.
     */
    public static final int INSTRUCTION_SIZE = 4;

    /**
     * The byte offset of the instruction opcode.
     */
    public static final int OPCODE_OFFSET = 0;

    /**
     * The byte offset of the result.
     */
    public static final int RESULT_OFFSET = 1;

    /**
     * The byte offset of the first operand.
     */
    public static final int OP1_OFFSET = 2;

    /**
     * The byte offset of the second operand.
     */
    public static final int OP2_OFFSET = 3;

    /**
     * The "constant" bit for the first operand of the instruction.
     */
    public static final short OP1_CONSTANT = 64;

    /**
     * The "constant" bit for the second operand of the instruction.
     */
    public static final short OP2_CONSTANT = 128;

    /**
     * The <code>ENERGY_COSTS</code> array stores the costs of each
     * instruction in energy units.
     */
    public static final short[] ENERGY_COSTS = {
        20,     // MAKE_ANT         0
        1,      // STONES           1
        1,      // OBSTACLES        2
        1,      // FOOD             3
        1,      // ANTS             4
        1,      // MARKS            5
        1,      // FOOD_AMOUNT      6
        1,      // STONE_NUMBER     7
        1,      // MARK_VALUE       8
        8,      // MOVE             9
        8,      // GET_STONE       10
        8,      // GET_FOOD        11
        8,      // PUT_STONE       12
        8,      // PUT_FOOD        13
        8,      // SET_MARK        14
        8,      // CLEAR_MARK      15
        2,      // COPY            16
        2,      // OR              17
        2,      // AND             18
        2,      // XOR             19
        2,      // NOT             20
        2,      // BITS_TRUE       21
        2,      // BITS_FALSE      22
        2,      // ADD             23
        2,      // SUB             24
        2,      // MULT            25
        2,      // DIV             26
        2,      // NEG             27
        2,      // EQUAL           28
        2,      // NOT_EQUAL       29
        2,      // LESS            30
        2,      // LESS_EQUAL      31
        2,      // GREATER         32
        2,      // GREATER_EQUAL   33
        1,      // GOTO            34
        1,      // GOTO_IF         35
    };

    /**
     * The maximum number of players.
     */
    public static final int MAX_PLAYERS = 4;

    /**
     * The maximum width and height of the playfield.
     */
    public static final int MAX_PLAYFIELD_SIZE = 1000;

    /**
     * The number of bits that are used to return environmental information
     * from instructions such as <i>Stones</i>.  Each bit represents the
     * status of a single cell in a certain direction from the ant position.
     */
    public static final int DIRECTION_BITS = 15;

    /**
     * The number of directions.
     */
    public static final int NUM_DIRECTIONS = 8;

    /**
     * The offset in x direction from the position of the ant, indexed by
     * direction and direction bit.
     */
    protected static final int[][] DIRECTION_X_INDEXES = {
        { -1,  0,  1, -2, -1,  0,  1,  2, -3, -2, -1,  0,  1,  2,  3 },
        {  0,  1,  1,  0,  1,  2,  2,  2,  0,  1,  2,  3,  3,  3,  3 },
        {  1,  1,  1,  2,  2,  2,  2,  2,  3,  3,  3,  3,  3,  3,  3 },
        {  1,  1,  0,  2,  2,  2,  1,  0,  3,  3,  3,  3,  2,  1,  0 },
        {  1,  0, -1,  2,  1,  0, -1, -2,  3,  2,  1,  0, -1, -2, -3 },
        {  0, -1, -1,  0, -1, -2, -2, -2,  0, -1, -2, -3, -3, -3, -3 },
        { -1, -1, -1, -2, -2, -2, -2, -2, -3, -3, -3, -3, -3, -3, -3 },
        { -1, -1,  0, -2, -2, -2, -1,  0, -3, -3, -3, -3, -2, -1,  0 },
    };

    /**
     * The offset in y direction from the position of the ant, indexed by
     * direction and direction bit.
     */
    protected static final int[][] DIRECTION_Y_INDEXES = {
        { -1, -1, -1, -2, -2, -2, -2, -2, -3, -3, -3, -3, -3, -3, -3 },
        { -1, -1,  0, -2, -2, -2, -1,  0, -3, -3, -3, -3, -2, -1,  0 },
        { -1,  0,  1, -2, -1,  0,  1,  2, -3, -2, -1,  0,  1,  2,  3 },
        {  0,  1,  1,  0,  1,  2,  2,  2,  0,  1,  2,  3,  3,  3,  3 },
        {  1,  1,  1,  2,  2,  2,  2,  2,  3,  3,  3,  3,  3,  3,  3 },
        {  1,  1,  0,  2,  2,  2,  1,  0,  3,  3,  3,  3,  2,  1,  0 },
        {  1,  0, -1,  2,  1,  0, -1, -2,  3,  2,  1,  0, -1, -2, -3 },
        {  0, -1, -1,  0, -1, -2, -2, -2,  0, -1, -2, -3, -3, -3, -3 },
    };

    /**
     * The near offset in x direction from the position of the ant, indexed by
     * direction.
     */
    protected static final int[] NEAR_DIRECTION_X_INDEXES = {
        0, 1, 1, 1, 0, -1, -1, -1
    };

    /**
     * The near offset in y direction from the position of the ant, indexed by
     * direction.
     */
    protected static final int[] NEAR_DIRECTION_Y_INDEXES = {
        -1, -1, 0, 1, 1, 1, 0, -1
    };

    /**
     * An array containing the names of the methods implementing the VM
     * instructions.
     */
    protected static final String[] INSTRUCTION_HANDLER_NAMES = {
        "iMakeAnt", "iStones", "iObstacles", "iFood", "iAnts", "iMarks",
        "iFoodAmount", "iStoneNumber", "iMarkValue", "iMove", "iGetStones",
        "iGetFood", "iPutStones", "iPutFood", "iSetMark", "iClearMark",
        "iCopy", "iOr", "iAnd", "iXor", "iNot", "iBitsTrue", "iBitsFalse",
        "iAdd", "iSub", "iMult", "iDiv", "iNeg", "iEqual", "iNotEqual",
        "iLess", "iLessEqual", "iGreater", "iGreaterEqual", "iGoto", "iGotoIf"
    };

    /**
     * An array of methods implementing the VM instructions.
     */
    protected static Method[] instructionHandlers = initInstructionHandlers();

    /**
     * Initializes the {@link #instructionHandlers} array.  The method uses the
     * Java reflection mechanism to obtain the <code>Method</code> objects that
     * represent the methods implementing the VM instructions.
     *
     * @return An array of <code>Method</code> objects, or <code>null</code>
     *         if a method could not be found.
     */
    protected static Method[] initInstructionHandlers() {

        Class<AntsVm> c = AntsVm.class;
        Method[] handlers = new Method[NUM_INSTRUCTIONS];

        try {
            for (int i = 0; i < NUM_INSTRUCTIONS; i++)
                handlers[i] = c.getDeclaredMethod(INSTRUCTION_HANDLER_NAMES[i]);

            return handlers;

        } catch (NoSuchMethodException e) {
            System.err.println("Error: Method '" + e.getMessage() + "' not found.");
            return null;
        }
    }

    private Random random = new Random();

    private int numberOfPlayers;
    private int playfieldWidth;
    private int playfieldHeight;
    private long sleepPerCycle;
    private short initialEnergy;
    private short energyPerFood;
    private short energyPerRun;
    private short maxFoodPerCell;
    private double foodRegrowRate;

    private String[] playerNames;

    private List<List<AntClass>> antClasses;
    private AntClass[] queenClasses;
    private Deque<Ant> activeAnts;

    private volatile boolean suspendRequested = false;
    private volatile boolean stopRequested = false;
    private volatile VmState state = VmState.CREATED;

    private PlayfieldCell[][] playfield;

    private int currentPlayer;
    private Ant currentAnt;
    private AntClass currentClass;
    private short[] program;
    private short[] variables;
    private int pc;
    private short instruction;
    private short opcode;
    private short result;
    private short op1;
    private short op2;

    /**
     * Returns the number of players.
     *
     * @return The number of players.
     */
    public int getNumberOfPlayers() {
        return numberOfPlayers;
    }

    /**
     * Returns the width of the playfield.
     *
     * @return The playfield width.
     */
    public int getPlayfieldWidth() {
        return playfieldWidth;
    }

    /**
     * Returns the height of the playfield.
     *
     * @return The playfield height.
     */
    public int getPlayfieldHeight() {
        return playfieldHeight;
    }

    /**
     * Copies a section of the playfield matrix to the specified array.
     *
     * @param dest The array of {@link PlayfieldCell} elements to which
     *             the section of the playfield matrix should copied.  The
     *             <code>dest</code> array must have the same dimensions as
     *             the playfield.
     * @param x The column where the copied section starts.
     * @param y The row where the copied section starts.
     * @param w The number of columns to copy.
     * @param h The number of rows to copy.
     */
    public void copyPlayfield(PlayfieldCell[][] dest, int x, int y, int w, int h) {

        for (int i = y; i < y + h; i++)
            for (int j = x; j < x + w; j++)
                dest[i][j] = playfield[i][j];
    }

    /**
     * Returns a single playfield cell.  The coordinates of the cell are
     * wrapped at the borders if they are outside the regular playfield.
     *
     * @param x The position in x direction.
     * @param y The position in y direction.
     * @return The playfield cell at the given position.
     */
    public PlayfieldCell getPlayfieldCell(int x, int y) {
        int x0 = (x >= 0) ?
            (x % playfieldWidth) :
            (x % playfieldWidth) + playfieldWidth;
        int y0 = (y >= 0) ?
            (y % playfieldHeight) :
            (y % playfieldHeight) + playfieldHeight;
        return playfield[y0][x0];
    }

    /**
     * Returns the ant classes of a player.  The first class in the returned
     * list is the queen class of the player.
     *
     * @param player The index of the player.
     * @return A list of {@link AntClass} objects representing the ant classes
     *         this player can use.
     */
    public List<AntClass> getAntClasses(int player) {
        return antClasses.get(player);
    }

    /**
     * Returns the queen class of a player.
     *
     * @param player The index of the player.
     * @return The queen class of the player.
     */
    public AntClass getQueenClass(int player) {
        return queenClasses[player];
    }

    /**
     * Creates a new <code>AntsVm</code> object.  The constructor sets all
     * configuration parameters, creates a playfield, the players, and loads
     * the ant classes.
     *
     * @param config The VM configuration object.
     * @throws ClassNotFoundException An ant class could not be found.
     * @throws IOException An I/O error occured while loading the ant classes.
     */
    public AntsVm(Configuration config)
        throws ClassNotFoundException, IOException {

        this.numberOfPlayers = config.numberOfPlayers;
        this.playfieldWidth  = config.playfieldWidth;
        this.playfieldHeight = config.playfieldHeight;
        this.sleepPerCycle   = config.sleepPerCycle;
        this.initialEnergy   = config.initialEnergy;
        this.energyPerFood   = config.energyPerFood;
        this.energyPerRun    = config.energyPerRun;
        this.maxFoodPerCell  = config.maxFoodPerCell;
        this.foodRegrowRate  = config.foodRegrowRate;

        createPlayfield(config);
        createPlayers(config);
        createAntClasses(config);

        activeAnts = new LinkedList<>();
    }

    /**
     * Loads all ant classes for all players.
     *
     * @param config The configuration of the VM.
     * @throws ClassNotFoundException A class to be loaded could not be found.
     * @throws IOException An I/O error occured while loading a class.
     */
    protected void createAntClasses(Configuration config)
        throws ClassNotFoundException, IOException {

        antClasses = new ArrayList<>(numberOfPlayers);
        queenClasses = new AntClass[numberOfPlayers];

        for (int i = 0; i < numberOfPlayers; i++)
            antClasses.add(new ArrayList<AntClass>());

        for (int i = 0; i < numberOfPlayers; i++) {

            Configuration.PlayerInfo info = config.playerInfos[i];
            Iterator<String> classFilesIt = info.classFiles.iterator();

            String queen = classFilesIt.next();
            loadQueenClass(i, config.dataPath + "/" + queen);

            while (classFilesIt.hasNext()) {
                String ant = classFilesIt.next();
                loadAntClass(i, config.dataPath + "/" + ant);
            }
        }
    }

    /**
     * Sets the player names.
     *
     * @param config The configuration of the VM.
     */
    protected void createPlayers(Configuration config) {

        playerNames = new String[numberOfPlayers];

        for (int i = 0; i < numberOfPlayers; i++)
            playerNames[i] = config.playerInfos[i].name;
    }

    /**
     * Creates the playfield using the {@link PlayfieldBuilder} class.
     *
     * @param config The configuration of the VM.
     */
    protected void createPlayfield(Configuration config) {

        PlayfieldBuilder builder = new PlayfieldBuilder();

        builder.stonesRatio      = config.stonesRatio;
        builder.foodRatio        = config.foodRatio;
        builder.passableRatio    = config.passableRatio;
        builder.maxStonesPerCell = config.maxStonesPerCell;
        builder.maxFoodPerCell   = config.maxFoodPerCell;

        playfield = builder.createPlayfield(
                numberOfPlayers, playfieldWidth, playfieldHeight);
    }

    /**
     * Loads the queen class for a player.
     *
     * @param player The index of the player.
     * @param filename The path to the ant class that is to be the queen class
     *                 of the player.
     * @throws ClassNotFoundException The class to be loaded could not be found.
     * @throws IOException An I/O error occured while loading the class.
     */
    protected void loadQueenClass(int player, String filename)
        throws IOException, ClassNotFoundException {

        AntClass queen = loadAntClass(player, filename);
        queenClasses[player] = queen;
    }

    /**
     * Loads an ant class for a player.
     *
     * @param player The index of the player.
     * @param filename The path to the ant class.
     * @throws ClassNotFoundException The class to be loaded could not be found.
     * @throws IOException An I/O error occured while loading the class.
     * @return The loaded ant class.
     */
    protected AntClass loadAntClass(int player, String filename)
        throws IOException, ClassNotFoundException {

        AntClass c;

        try (FileInputStream fs = new FileInputStream(filename);
             ObjectInputStream os = new ObjectInputStream(fs)) {

            c = (AntClass) os.readObject();
        }

        c.setPlayer(player);
        antClasses.get(player).add(c);
        return c;
    }

    /**
     * Internal method to start the VM.  Clients should <em>never</em>
     * directly call this method or the <code>start()</code> method inherited
     * from the <code>Thread</code>class.  It is called implicitly as a
     * consequence of calling {@link #startVm()}.
     */
    public void run() {

        suspendRequested = false;
        stopRequested = false;
        state = VmState.RUNNING;

        while (!stopRequested) {

            if (activeAnts.size() == 0) {
                state = VmState.STOPPED_BY_SIM;
                break;
            }

            runCurrentAnt();
            regrowFood();

            try {
                if (sleepPerCycle > 0)
                    sleep(sleepPerCycle);

                if (suspendRequested) {
                    state = VmState.SUSPENDED;
                    synchronized (this) {
                        while (suspendRequested && !stopRequested)
                            wait();
                    }
                    state = VmState.RUNNING;
                }

            } catch (InterruptedException e) {
                state = VmState.TERMINATED;
                break;
            }
        }

        if (stopRequested)
            state = VmState.STOPPED_BY_COMMAND;
    }

    /**
     * Creates a new thread that runs the VM.
     */
    public void startVm() {

        for (int i = 0; i < numberOfPlayers; i++) {

            short x = 0;
            short y = 0;
            boolean emptyCellFound = false;

            while (!emptyCellFound) {
                x = (short) random.nextInt(playfieldWidth);
                y = (short) random.nextInt(playfieldHeight);
                if (playfield[y][x].isEmpty()) {
                    emptyCellFound = true;
                }
            }

            Ant queen = new Ant(queenClasses[i], x, y, (short) i, initialEnergy);
            playfield[y][x].ant = queen;
            activeAnts.add(queen);
        }

        start();
    }

    /**
     * Notifies the VM thread that it should be stopped.  After calling this
     * method a client should wait until {@link #getVmState()} returns one of
     * the states <code>VmState.STOPPED_BY_COMMAND</code>,
     * <code>VmState.STOPPED_BY_SIM</code>, or
     * <code>VmState.TERMINATED</code>.
     */
    public void stopVm() {
        stopRequested = true;
    }

    /**
     * Notifies the VM thread that it should be haltet temporally.  After
     * calling this method a client should wait until {@link #getVmState()}
     * returns one of the states <code>VmState.SUSPENDED</code>,
     * <code>VmState.STOPPED_BY_COMMAND</code>,
     * <code>VmState.STOPPED_BY_SIM</code>, or
     * <code>VmState.TERMINATED</code>.  If the VM enters the
     * <code>VmState.SUSPENDED</code> state, its execution can be continued by
     * calling {@link #resumeVm()}.
     */
    public void suspendVm() {
        suspendRequested = true;
    }

    /**
     * Restarts the VM.  This method should only be called if the VM has been
     * haltet through a call to {@link #suspendVm()} and has reached the
     * <code>VmState.SUSPENDED</code> state.
     */
    public void resumeVm() {
        suspendRequested = false;
        synchronized(this) {
            notifyAll();
        }
    }

    /**
     * Returns the state of the VM.
     *
     * @return The VM state.
     */
    public VmState getVmState() {
        return state;
    }

    /**
     * Grows new food on the playfield.
     */
    protected void regrowFood() {

        if (random.nextDouble() < foodRegrowRate) {

            int x = random.nextInt(playfieldWidth);
            int y = random.nextInt(playfieldHeight);

            PlayfieldCell cell = playfield[y][x];

            if (cell.isEmpty() || cell.food > 0)
                cell.food += random.nextInt(maxFoodPerCell);
        }
    }

    /**
     * Executes a cycle for the current ant.  A single cycle is bounded by the
     * energy that an ant is given per cycle.
     */
    protected void runCurrentAnt() {

        currentAnt = activeAnts.removeFirst();
        variables = currentAnt.getVariables();

        currentClass = currentAnt.getAntClass();
        currentPlayer = currentClass.getPlayer();
        program = currentClass.getProgram();

        int energyLeft = energyPerRun;
        boolean antDied = false;

        while (true) {

            pc = currentAnt.getPC();

            instruction = program[pc + OPCODE_OFFSET];
            result = program[pc + RESULT_OFFSET];
            op1 = program[pc + OP1_OFFSET];
            op2 = program[pc + OP2_OFFSET];

            opcode = (short) (instruction & ~(OP1_CONSTANT | OP2_CONSTANT));

            short instructionCosts = ENERGY_COSTS[opcode];

            if (energyLeft < instructionCosts)
                break;

            if (currentAnt.getEnergy() < instructionCosts) {

                int energyNeeded =
                    (instructionCosts - currentAnt.getEnergy());
                int foodNeeded =
                    (energyNeeded + energyPerFood - 1) / energyPerFood;

                if (currentAnt.getFood() >= foodNeeded) {
                    variables[Ant.MY_FOOD] -= foodNeeded;
                    variables[Ant.MY_ENERGY] += foodNeeded * energyPerFood;
                } else {
                    antDied = true;
                    break;
                }
            }

            energyLeft -= instructionCosts;
            variables[Ant.MY_ENERGY] -= instructionCosts;

            try {
                instructionHandlers[opcode].invoke(this);
            } catch (IllegalAccessException e) {
                // This should not happen
                System.exit(-1);
            } catch (InvocationTargetException e) {
                // This should not happen
                System.exit(-1);
            }
        }

        if (antDied == false)
            activeAnts.addLast(currentAnt);
        else
            playfield[currentAnt.getYPos()][currentAnt.getXPos()].ant = null;
    }

    /**
     * Returns the value of the first operand of the current instruction.  If
     * the first operand is a variable, its value is taken from the variables
     * of the current ant.
     *
     * @return The value of the first operand.
     */
    protected short getOp1Value() {

        if ((instruction & OP1_CONSTANT) != 0)
            return op1;
        else
            return variables[op1];
    }

    /**
     * Returns the value of the second operand of the current instruction.  If
     * the second operand is a variable, its value is taken from the variables
     * of the current ant.
     *
     * @return The value of the first operand.
     */
    protected short getOp2Value() {

        if ((instruction & OP2_CONSTANT) != 0)
            return op2;
        else
            return variables[op2];
    }

    /**
     * Sets the result value of the current instruction.  If the result
     * variable must not be set because it is a system variable, this is
     * slightly ignored.
     *
     * @param value The result value.
     */
    protected void setResult(short value) {
        if (result >= FIRST_USER_VAR)
            variables[result] = value;
    }

    /**
     * The <i>MakeAnt</i> instruction.
     */
    protected void iMakeAnt() {

        if (currentClass == queenClasses[currentPlayer]) {

            short classId = getOp1Value();
            int x0 = currentAnt.getXPos();
            int y0 = currentAnt.getYPos();

            int x = 0;
            int y = 0;

            PlayfieldCell cell = null;
            boolean emptyCellFound = false;

            for (int i = 0; i < NUM_DIRECTIONS; i++) {

                x = x0 + NEAR_DIRECTION_X_INDEXES[i];
                y = y0 + NEAR_DIRECTION_Y_INDEXES[i];

                cell = getPlayfieldCell(x, y);

                if (cell.isEmpty()) {
                    emptyCellFound = true;
                    break;
                }
            }

            if (emptyCellFound) {

                for (AntClass c: antClasses.get(currentPlayer)) {

                    if (c.getId() == classId) {
                        short food = currentAnt.getFood();
                        if (food >= c.getBackpackSize()) {
                            Ant newAnt = new Ant(c, x, y, currentAnt.getTribe(), initialEnergy);
                            cell.ant = newAnt;
                            activeAnts.addLast(newAnt);
                            currentAnt.setFood((short) (food - c.getBackpackSize()));
                            break;
                        }
                    }
                }
            }
        }

        currentAnt.nextInstruction();
    }

    /**
     * The <i>Stones</i> instruction.
     */
    protected void iStones() {

        short direction = getOp1Value();
        int xpos = currentAnt.getXPos();
        int ypos = currentAnt.getYPos();

        short stones = 0;

        for (int i = 0; i < DIRECTION_BITS; i++) {

            int dx = DIRECTION_X_INDEXES[direction][i];
            int dy = DIRECTION_Y_INDEXES[direction][i];

            PlayfieldCell cell = getPlayfieldCell(xpos + dx, ypos + dy);

            if (cell.stones > 0)
                stones = (short) (stones | (1 << i));
        }

        setResult(stones);
        currentAnt.nextInstruction();
    }

    /**
     * The <i>Obstacles</i> instruction.
     */
    protected void iObstacles() {

        short direction = getOp1Value();
        int xpos = currentAnt.getXPos();
        int ypos = currentAnt.getYPos();

        short obstacles = 0;

        for (int i = 0; i < DIRECTION_BITS; i++) {

            int dx = DIRECTION_X_INDEXES[direction][i];
            int dy = DIRECTION_Y_INDEXES[direction][i];

            PlayfieldCell cell = getPlayfieldCell(xpos + dx, ypos + dy);

            if (!cell.isPassable)
                obstacles = (short) (obstacles | (1 << i));
        }

        setResult(obstacles);
        currentAnt.nextInstruction();
    }

    /**
     * The <i>Food</i> instruction.
     */
    protected void iFood() {

        short direction = getOp1Value();
        int xpos = currentAnt.getXPos();
        int ypos = currentAnt.getYPos();

        short food = 0;

        for (int i = 0; i < DIRECTION_BITS; i++) {

            int dx = DIRECTION_X_INDEXES[direction][i];
            int dy = DIRECTION_Y_INDEXES[direction][i];

            PlayfieldCell cell = getPlayfieldCell(xpos + dx, ypos + dy);

            if (cell.food > 0)
                food = (short) (food | (1 << i));
        }

        setResult(food);
        currentAnt.nextInstruction();
    }

    /**
     * The <i>Ants</i> instruction.
     */
    protected void iAnts() {

        short direction = getOp1Value();
        short tribe = getOp2Value();
        int xpos = currentAnt.getXPos();
        int ypos = currentAnt.getYPos();

        short ants = 0;

        for (int i = 0; i < DIRECTION_BITS; i++) {

            int dx = DIRECTION_X_INDEXES[direction][i];
            int dy = DIRECTION_Y_INDEXES[direction][i];

            PlayfieldCell cell = getPlayfieldCell(xpos + dx, ypos + dy);

            boolean antHere = false;

            if (cell.ant != null) {

                if (tribe == Tribe.ANY)
                    antHere = true;
                else if (tribe == Tribe.OTHER)
                    antHere = cell.ant.getTribe() != currentAnt.getTribe();
                else if (tribe == Tribe.OUR)
                    antHere = cell.ant.getTribe() == currentAnt.getTribe();
                else
                    antHere = cell.ant.getTribe() == tribe;
            }

            if (antHere == true)
                ants = (short) (ants | (1 << i));
        }

        setResult(ants);
        currentAnt.nextInstruction();
    }

    /**
     * The <i>Marks</i> instruction.
     */
    protected void iMarks() {

        short direction = getOp1Value();
        short tribe = getOp2Value();
        int xpos = currentAnt.getXPos();
        int ypos = currentAnt.getYPos();

        short marks = 0;

        for (int i = 0; i < DIRECTION_BITS; i++) {

            int dx = DIRECTION_X_INDEXES[direction][i];
            int dy = DIRECTION_Y_INDEXES[direction][i];

            PlayfieldCell cell = getPlayfieldCell(xpos + dx, ypos + dy);

            boolean markHere = false;

            if (tribe == Tribe.ANY) {
                for (int j = 0; j < numberOfPlayers; j++) {
                    if (cell.marks[j] != 0) {
                        markHere = true;
                        break;
                    }
                }
            } else if (tribe == Tribe.OTHER) {
                for (int j = 0; j < numberOfPlayers; j++) {
                    if (cell.marks[j] != 0 && j != currentAnt.getTribe()) {
                        markHere = true;
                        break;
                    }
                }
            } else if (tribe == Tribe.OUR) {
                markHere = cell.marks[currentAnt.getTribe()] != 0;
            } else {
                markHere = cell.marks[tribe] != 0;
            }

            if (markHere == true)
                marks = (short) (marks | (1 << i));
        }

        setResult(marks);
        currentAnt.nextInstruction();
    }

    /**
     * The <i>FoodAmount</i> instruction.
     */
    protected void iFoodAmount() {

        short direction = getOp1Value();
        int x = currentAnt.getXPos() + NEAR_DIRECTION_X_INDEXES[direction];
        int y = currentAnt.getYPos() + NEAR_DIRECTION_Y_INDEXES[direction];

        setResult(getPlayfieldCell(x, y).food);
        currentAnt.nextInstruction();
    }

    /**
     * The <i>StoneNumber</i> instruction.
     */
    protected void iStoneNumber() {

        short direction = getOp1Value();
        int x = currentAnt.getXPos() + NEAR_DIRECTION_X_INDEXES[direction];
        int y = currentAnt.getYPos() + NEAR_DIRECTION_Y_INDEXES[direction];

        setResult(getPlayfieldCell(x, y).stones);
        currentAnt.nextInstruction();
    }

    /**
     * The <i>MarkValue</i> instruction.
     */
    protected void iMarkValue() {

        short direction = getOp1Value();
        short tribe = getOp2Value();
        int x = currentAnt.getXPos() + NEAR_DIRECTION_X_INDEXES[direction];
        int y = currentAnt.getYPos() + NEAR_DIRECTION_Y_INDEXES[direction];

        setResult(getPlayfieldCell(x, y).marks[tribe]);
        currentAnt.nextInstruction();
    }

    /**
     * The <i>Move</i> instruction.
     */
    protected void iMove() {

        short direction = getOp1Value();
        int x0 = currentAnt.getXPos();
        int y0 = currentAnt.getYPos();
        int x  = x0 + NEAR_DIRECTION_X_INDEXES[direction];
        int y  = y0 + NEAR_DIRECTION_Y_INDEXES[direction];

        if (x < 0)
            x = (x % playfieldWidth) + playfieldWidth;
        else if (x >= playfieldWidth)
            x = x % playfieldWidth;

        if (y < 0)
            y = (y % playfieldHeight) + playfieldHeight;
        else if (y >= playfieldHeight)
            y = y % playfieldHeight;

        if (getPlayfieldCell(x, y).isEmpty()) {
            getPlayfieldCell(x, y).ant = currentAnt;
            getPlayfieldCell(x0, y0).ant = null;
            currentAnt.setPos(x, y);
            setResult((short) 0);
        } else {
            setResult((short) 1);
        }

        currentAnt.nextInstruction();
    }

    /**
     * The <i>GetStones</i> instruction.
     */
    protected void iGetStones() {

        short direction = getOp1Value();
        short stonesToGet = getOp2Value();
        int x = currentAnt.getXPos() + NEAR_DIRECTION_X_INDEXES[direction];
        int y = currentAnt.getYPos() + NEAR_DIRECTION_Y_INDEXES[direction];;

        PlayfieldCell cell = getPlayfieldCell(x, y);

        if (stonesToGet > cell.stones ||
            stonesToGet > currentAnt.getBackpackSpace()) {
            stonesToGet = (short) Math.min(cell.stones, currentAnt.getBackpackSpace());
            setResult((short) 1);
        } else {
            setResult((short) 0);
        }

        cell.stones -= stonesToGet;
        variables[Ant.MY_STONES] += stonesToGet;
        currentAnt.nextInstruction();
    }

    /**
     * The <i>GetFood</i> instruction.
     */
    protected void iGetFood() {

        short direction = getOp1Value();
        short foodToGet = getOp2Value();
        int x = currentAnt.getXPos() + NEAR_DIRECTION_X_INDEXES[direction];
        int y = currentAnt.getYPos() + NEAR_DIRECTION_Y_INDEXES[direction];

        PlayfieldCell cell = getPlayfieldCell(x, y);

        if (foodToGet > cell.food ||
            foodToGet > currentAnt.getBackpackSpace()) {
            foodToGet = (short) Math.min(cell.food, currentAnt.getBackpackSpace());
            setResult((short) 1);
        } else {
            setResult((short) 0);
        }

        cell.food -= foodToGet;
        variables[Ant.MY_FOOD] += foodToGet;
        currentAnt.nextInstruction();
    }

    /**
     * The <i>PutStones</i> instruction.
     */
    protected void iPutStones() {

        short direction = getOp1Value();
        short stonesToPut = getOp2Value();
        int x = currentAnt.getXPos() + NEAR_DIRECTION_X_INDEXES[direction];
        int y = currentAnt.getYPos() + NEAR_DIRECTION_Y_INDEXES[direction];;

        PlayfieldCell cell = getPlayfieldCell(x, y);

        if (cell.isPassable && cell.ant == null && cell.food == 0) {
            if (stonesToPut > variables[Ant.MY_STONES]) {
                stonesToPut = variables[Ant.MY_STONES];
                setResult((short) 1);
            } else {
                setResult((short) 0);
            }
            variables[Ant.MY_STONES] -= stonesToPut;
            cell.stones += stonesToPut;
        } else {
            setResult((short) 1);
        }

        currentAnt.nextInstruction();
    }

    /**
     * The <i>PutFood</i> instruction.
     */
    protected void iPutFood() {

        short direction = getOp1Value();
        short foodToPut = getOp2Value();
        int x = currentAnt.getXPos() + NEAR_DIRECTION_X_INDEXES[direction];
        int y = currentAnt.getYPos() + NEAR_DIRECTION_Y_INDEXES[direction];;

        PlayfieldCell cell = getPlayfieldCell(x, y);

        if (cell.isPassable && cell.ant == null && cell.stones == 0) {
            if (foodToPut > variables[Ant.MY_FOOD]) {
                foodToPut = variables[Ant.MY_FOOD];
                setResult((short) 1);
            } else {
                setResult((short) 0);
            }
            variables[Ant.MY_FOOD] -= foodToPut;
            cell.food += foodToPut;
        } else {
            setResult((short) 1);
        }

        currentAnt.nextInstruction();
    }

    /**
     * The <i>SetMark</i> instruction.
     */
    protected void iSetMark() {

        short direction = getOp1Value();
        short value = getOp2Value();
        int x = currentAnt.getXPos() + NEAR_DIRECTION_X_INDEXES[direction];
        int y = currentAnt.getYPos() + NEAR_DIRECTION_Y_INDEXES[direction];;

        PlayfieldCell cell = getPlayfieldCell(x, y);
        cell.marks[currentAnt.getTribe()] = value;

        currentAnt.nextInstruction();
    }

    /**
     * The <i>ClearMark</i> instruction.
     */
    protected void iClearMark() {

        short direction = getOp1Value();
        int x = currentAnt.getXPos() + NEAR_DIRECTION_X_INDEXES[direction];
        int y = currentAnt.getYPos() + NEAR_DIRECTION_Y_INDEXES[direction];;

        PlayfieldCell cell = getPlayfieldCell(x, y);
        cell.marks[currentAnt.getTribe()] = 0;

        currentAnt.nextInstruction();
    }

    /**
     * The <i>Copy</i> instruction.
     */
    protected void iCopy() {

        setResult(getOp1Value());
        currentAnt.nextInstruction();
    }

    /**
     * The <i>Or</i> instruction.
     */
    protected void iOr() {

        short x = getOp1Value();
        short y = getOp2Value();
        setResult((short) (x | y));
        currentAnt.nextInstruction();
    }

    /**
     * The <i>And</i> instruction.
     */
    protected void iAnd() {

        short x = getOp1Value();
        short y = getOp2Value();
        setResult((short) (x & y));
        currentAnt.nextInstruction();
    }

    /**
     * The <i>Xor</i> instruction.
     */
    protected void iXor() {

        short x = getOp1Value();
        short y = getOp2Value();
        setResult((short) (x ^ y));
        currentAnt.nextInstruction();
    }

    /**
     * The <i>Not</i> instruction.
     */
    protected void iNot() {

        short x = getOp1Value();
        setResult((short) (~x));
        currentAnt.nextInstruction();
    }

    /**
     * The <i>BitsTrue</i> instruction.
     */
    protected void iBitsTrue() {

        short x = getOp1Value();
        short bits = 0;
        for (int i = 0; i < 16; i++) {
            if ((x & (1 << i)) != 0)
                ++bits;
        }
        setResult(bits);
        currentAnt.nextInstruction();
    }

    /**
     * The <i>BitsFalse</i> instruction.
     */
    protected void iBitsFalse() {

        short x = getOp1Value();
        short bits = 0;
        for (int i = 0; i < 16; i++) {
            if ((x & (1 << i)) == 0)
                ++bits;
        }
        setResult(bits);
        currentAnt.nextInstruction();
    }

    /**
     * The <i>Add</i> instruction.
     */
    protected void iAdd() {

        short v1 = getOp1Value();
        short v2 = getOp2Value();
        setResult((short) (v1 + v2));
        currentAnt.nextInstruction();
    }

    /**
     * The <i>Sub</i> instruction.
     */
    protected void iSub() {

        short v1 = getOp1Value();
        short v2 = getOp2Value();
        setResult((short) (v1 - v2));
        currentAnt.nextInstruction();
    }

    /**
     * The <i>Mult</i> instruction.
     */
    protected void iMult() {

        short v1 = getOp1Value();
        short v2 = getOp2Value();
        setResult((short) (v1 * v2));
        currentAnt.nextInstruction();
    }

    /**
     * The <i>Div</i> instruction.
     */
    protected void iDiv() {

        short v1 = getOp1Value();
        short v2 = getOp2Value();
        setResult((short) (v1 / v2));
        currentAnt.nextInstruction();
    }

    /**
     * The <i>Neg</i> instruction.
     */
    protected void iNeg() {

        short v1 = getOp1Value();
        setResult((short) (-v1));
        currentAnt.nextInstruction();
    }

    /**
     * The <i>Equal</i> instruction.
     */
    protected void iEqual() {

        short v1 = getOp1Value();
        short v2 = getOp2Value();
        setResult((short) (v1 == v2 ? 1 : 0));
        currentAnt.nextInstruction();
    }

    /**
     * The <i>NotEqual</i> instruction.
     */
    protected void iNotEqual() {

        short v1 = getOp1Value();
        short v2 = getOp2Value();
        setResult((short) (v1 != v2 ? 1 : 0));
        currentAnt.nextInstruction();
    }

    /**
     * The <i>Less</i> instruction.
     */
    protected void iLess() {

        short v1 = getOp1Value();
        short v2 = getOp2Value();
        setResult((short) (v1 < v2 ? 1 : 0));
        currentAnt.nextInstruction();
    }

    /**
     * The <i>LessEqual</i> instruction.
     */
    protected void iLessEqual() {

        short v1 = getOp1Value();
        short v2 = getOp2Value();
        setResult((short) (v1 <= v2 ? 1 : 0));
        currentAnt.nextInstruction();
    }

    /**
     * The <i>Greater</i> instruction.
     */
    protected void iGreater() {

        short v1 = getOp1Value();
        short v2 = getOp2Value();
        setResult((short) (v1 > v2 ? 1 : 0));
        currentAnt.nextInstruction();
    }

    /**
     * The <i>GreaterEqual</i> instruction.
     */
    protected void iGreaterEqual() {

        short v1 = getOp1Value();
        short v2 = getOp2Value();
        setResult((short) (v1 >= v2 ? 1 : 0));
        currentAnt.nextInstruction();
    }

    /**
     * The <i>Goto</i> instruction.
     */
    protected void iGoto() {

        short address = getOp1Value();
        currentAnt.setPC(INSTRUCTION_SIZE * address);
    }

    /**
     * The <i>GotoIf</i> instruction.
     */
    protected void iGotoIf() {

        short address = getOp1Value();
        short cond = getOp2Value();

        if (cond != 0)
            currentAnt.setPC(INSTRUCTION_SIZE * address);
        else
            currentAnt.nextInstruction();
    }
}
