package ants.vm;

/**
 * The <code>Instruction</code> class defines the numeric codes for all VM
 * instructions.
 */
public class Instruction {

    public static final short MAKE_ANT      = 0;
    public static final short STONES        = 1;
    public static final short OBSTACLES     = 2;
    public static final short FOOD          = 3;
    public static final short ANTS          = 4;
    public static final short MARKS         = 5;
    public static final short FOOD_AMOUNT   = 6;
    public static final short STONE_NUMBER  = 7;
    public static final short MARK_VALUE    = 8;
    public static final short MOVE          = 9;
    public static final short GET_STONES    = 10;
    public static final short GET_FOOD      = 11;
    public static final short PUT_STONES    = 12;
    public static final short PUT_FOOD      = 13;
    public static final short SET_MARK      = 14;
    public static final short CLEAR_MARK    = 15;
    public static final short COPY          = 16;
    public static final short OR            = 17;
    public static final short AND           = 18;
    public static final short XOR           = 19;
    public static final short NOT           = 20;
    public static final short BITS_TRUE     = 21;
    public static final short BITS_FALSE    = 22;
    public static final short ADD           = 23;
    public static final short SUB           = 24;
    public static final short MULT          = 25;
    public static final short DIV           = 26;
    public static final short NEG           = 27;
    public static final short EQUAL         = 28;
    public static final short NOT_EQUAL     = 29;
    public static final short LESS          = 30;
    public static final short LESS_EQUAL    = 31;
    public static final short GREATER       = 32;
    public static final short GREATER_EQUAL = 33;
    public static final short GOTO          = 34;
    public static final short GOTO_IF       = 35;
}
