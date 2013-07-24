package ants.compiler;

import java.util.*;
import java.io.*;
import ants.vm.AntClass;
import ants.vm.AntsVm;
import ants.vm.Instruction;
import ants.vm.Direction;

/**
 * The <code>AntsCompiler</code> class provides the compiler of the Ants
 * system.  It translates source files from Ants assembly code to binary
 * representations of these ants, which can then be loaded into the Ants
 * system.
 */
public class AntsCompiler {

    /**
     * Maps instruction identifiers to their numeric opcodes.
     */
    protected static HashMap<String, Short> instructionMap;

    /**
     * Maps constant identifiers (without leading '#') to their numeric
     * values.
     */
    protected static HashMap<String, Short> constantMap;

    /**
     * Instruction identifiers.
     */
    public static final String[] instructions = {
        "MakeAnt", "Stones", "Obstacles", "Food", "Ants", "Marks",
        "FoodAmount", "StoneNumber", "MarkValue", "Move", "GetStones",
        "GetFood", "PutStones", "PutFood", "SetMark", "CleanMark",
        "Copy", "Or", "And", "Xor", "Not", "BitsTrue", "BitsFalse",
        "Add", "Sub", "Mult", "Div", "Neg", "Equal", "NotEqual",
        "Less", "LessEqual", "Greater", "GreaterEqual", "Goto", "GotoIf"
    };

    /**
     * Tribe constant identifiers.
     */
    public static final String[] tribes = {
        "Red", "Green", "Blue", "Yellow", "Any", "Other", "Our"
    };

    /**
     * Direction constant identifiers.
     */
    public static final String[] directions = {
        "North", "NorthEast", "East", "SouthEast",
        "South", "SouthWest", "West", "NorthWest",
        "Here"
    };

    /**
     * Default variable identifiers.
     */
    public static final String[] defaultVariables = {
        "MyBackpackSize", "MyFood", "MyStones", "MyEnergy", "MyTribe"
    };

    static {

        instructionMap = new HashMap<>();

        for (short i = 0; i < instructions.length; i++)
            instructionMap.put(instructions[i], i);

        constantMap = new HashMap<>();

        for (short i = 0; i < tribes.length; i++)
            constantMap.put(tribes[i], i);

        for (short i = 0; i < directions.length; i++)
            constantMap.put(directions[i], i);
    }

    /**
     * The lexical analyzer of the compiler.
     */
    protected Lexer lexer;

    /**
     * Maps variable identifiers to {@link VariableEntry} objects.
     */
    protected HashMap<String, VariableEntry> variables;

    /**
     * Maps label identifiers to {@link LabelEntry} objects.
     */
    protected HashMap<String, LabelEntry> labels;

    /**
     * The program counter.
     */
    protected short pc;

    /**
     * The variable counter.
     */
    protected short vc;

    /**
     * The {@link AntClass} object that is constructed from the source file.
     */
    protected AntClass ant;

    /**
     * The compiled program of the ant class.
     */
    protected short[] program;

    /**
     * Tests the current token for an expected token and reads the next symbol
     * if they match.
     *
     * @param t The expected token.
     * @throws SyntaxError The expected token and the current token do not
     *                     match.
     * @throws IOException An I/O error occured.
     */
    protected void getToken(Lexer.Token t) throws SyntaxError, IOException {

        if (lexer.token != t) {

            String s;

            switch (t) {
                case EOF:           s = "end of file"; break;
                case OR:            s = "'|'"; break;
                case AND:           s = "'&'"; break;
                case XOR:           s = "'^'"; break;
                case PLUS:          s = "'+'"; break;
                case MINUS:         s = "'-'"; break;
                case MULT:          s = "'*'"; break;
                case DIV:           s = "'/'"; break;
                case NOT:           s = "'!'"; break;
                case EQUAL:         s = "'=='"; break;
                case NOT_EQUAL:     s = "'!='"; break;
                case LESS:          s = "'<'"; break;
                case LESS_EQUAL:    s = "'<='"; break;
                case GREATER:       s = "'>'"; break;
                case GREATER_EQUAL: s = "'>='"; break;
                case VARIABLE:      s = "variable"; break;
                case CONSTANT:      s = "constant"; break;
                case LABEL:         s = "label"; break;
                case NUMBER:        s = "number"; break;
                case IDENTIFIER:    s = "identifier"; break;
                case DEFINE_ANT:    s = "'DefineAnt'"; break;
                case CONFIGURATION: s = "'Configuration'"; break;
                case PROGRAM:       s = "'Program'"; break;
                case LPARENT:       s = "'('"; break;
                case RPARENT:       s = "'('"; break;
                case COLON:         s = "':'"; break;
                case COMMA:         s = "','"; break;
                case ASSIGN:        s = "'='"; break;
                default:            s = "something else"; break;
            }

            throw new SyntaxError(lexer, "Expected " + s + ".");
        }
        else {
            lexer.getNextToken();
        }
    }

    /**
     * Tests the current token for an identifier and reads the next symbol.
     *
     * @return The string value of the identifier.
     * @throws SyntaxError The current token is not an identifier.
     * @throws IOException An I/O error occured.
     */
    protected String getIdentifier() throws SyntaxError, IOException {

        if (lexer.token != Lexer.Token.IDENTIFIER)
            throw new SyntaxError(lexer, "Identifier expected.");

        String ident = lexer.stringValue;
        lexer.getNextToken();
        return ident;
    }

    /**
     * Tests the curren token for a variable reference and reads the next
     * token.
     *
     * @return The variable identifier.
     * @throws SyntaxError The current token is not a variable reference.
     * @throws IOException An I/O error occured.
     */
    protected String getVariable() throws SyntaxError, IOException {

        if (lexer.token != Lexer.Token.VARIABLE)
            throw new SyntaxError(lexer, "Variable expected.");

        String ident = lexer.stringValue;
        lexer.getNextToken();
        return ident;
    }

    /**
     * Tests the current token for a label reference and reads the next token.
     *
     * @return The label identifier.
     * @throws SyntaxError The current token is not a label reference.
     * @throws IOException An I/O error occured.
     */
    protected String getLabel() throws SyntaxError, IOException {

        if (lexer.token != Lexer.Token.LABEL)
            throw new SyntaxError(lexer, "Label expected.");

        String ident = lexer.stringValue;
        lexer.getNextToken();
        return ident;
    }

    /**
     * Tests the current token for a number literal and reads the next token.
     *
     * @return The numeric value of the number literal.
     * @throws SyntaxError The current token is not a number literal.
     * @throws IOException An I/O error occured.
     */
    protected short getNumber() throws SyntaxError, IOException {

        if (lexer.token != Lexer.Token.NUMBER)
            throw new SyntaxError(lexer, "Number expected.");

        short num = lexer.numValue;
        lexer.getNextToken();
        return num;
    }

    /**
     * Tests the current token for a constant reference and reads the next
     * token.
     *
     * @return The numeric value of the constant.
     * @throws SyntaxError The current token is not a valid constant
     *                     reference.
     * @throws IOException An I/O error occured.
     */
    protected short getConstant() throws SyntaxError, IOException {

        if (lexer.token != Lexer.Token.CONSTANT)
            throw new SyntaxError(lexer, "Constant expected.");

        Short val = constantMap.get(lexer.stringValue);

        if (val == null)
            throw new SyntaxError(lexer, "'#" + lexer.stringValue +
                    "' is not a valid constant.");

        lexer.getNextToken();
        return val.shortValue();
    }

    /**
     * Tests the current token for an instruction identifier and reads the
     * next token.
     *
     * @return The numerical opcode of the instruction.
     * @throws SyntaxError The current token is not a valid instruction
     *                     identifier.
     * @throws IOException An I/O error occured.
     */
    protected short getInstruction() throws SyntaxError, IOException {

        String ident = getIdentifier();
        Short opcode = instructionMap.get(ident);

        if (opcode == null)
            throw new SyntaxError(lexer, "'" + ident +
                    "' is no valid instruction.");

        return opcode.shortValue();
    }

    /**
     * Parses a whole ant source file.  A valid source file consists of a
     * header, a configuration section, and the ant program.  See the file
     * <code>Spec.txt</code> for a definition of the Ant syntax.
     *
     * @throws SyntaxError The source file does not contain a valid
     *                     description of an ant class.
     * @throws IOException An I/O error occured.
     */
    protected void parseAnt() throws SyntaxError, IOException {

        lexer.getNextToken();

        parseHeader();
        parseConfiguration();
        parseProgram();
    }

    /**
     * Parses the header of an ant source file.
     *
     * @throws SyntaxError The source file does not contain a valid header.
     * @throws IOException An I/O error occured.
     */
    protected void parseHeader() throws SyntaxError, IOException {

        getToken(Lexer.Token.DEFINE_ANT);
        ant.setName(getIdentifier());
        getToken(Lexer.Token.LPARENT);
        ant.setId(getNumber());
        getToken(Lexer.Token.RPARENT);
        getToken(Lexer.Token.COLON);
    }

    /**
     * Parses the configuration section of an ant source file.
     *
     * @throws SyntaxError The source file does not contain a valid
     *                     configuration section.
     * @throws IOException An I/O error occured.
     */
    protected void parseConfiguration() throws SyntaxError, IOException {

        getToken(Lexer.Token.CONFIGURATION);
        getToken(Lexer.Token.COLON);

        if (!getVariable().equals("MyBackpackSize"))
            throw new SyntaxError(lexer,
                    "Only $MyBackpackSize can be set in the configuration section.");

        getToken(Lexer.Token.ASSIGN);
        ant.setBackpackSize(getNumber());
    }

    /**
     * Parses the program section of an ant source file.
     *
     * @throws SyntaxError The source file does not contain a valid program.
     * @throws IOException An I/O error occured.
     */
    protected void parseProgram() throws SyntaxError, IOException {

        getToken(Lexer.Token.PROGRAM);
        getToken(Lexer.Token.COLON);

        while (lexer.token == Lexer.Token.LABEL
            || lexer.token == Lexer.Token.VARIABLE
            || lexer.token == Lexer.Token.IDENTIFIER) {

            if (lexer.token == Lexer.Token.LABEL)
                parseLabelDefinition();
            else if (lexer.token == Lexer.Token.VARIABLE)
                parseAssignInstruction();
            else if (lexer.token == Lexer.Token.IDENTIFIER)
                parseFunctionalInstruction();
        }

        getToken(Lexer.Token.EOF);
    }

    /**
     * Parses a variable reference.
     *
     * @return A {@link VariableEntry} that represents the parsed variable.
     * @throws SyntaxError The next symbol in the source file is not a valid
     *                     variable reference.
     * @throws IOException An I/O error occured.
     */
    protected VariableEntry parseVariableReference()
        throws SyntaxError, IOException {

        String ident = getVariable();
        VariableEntry var = variables.get(ident);

        if (var == null) {
            var = new VariableEntry(ident, vc++);
            variables.put(ident, var);
        }

        return var;
    }

    /**
     * Parses a label reference.
     *
     * @param offset The byte offset from the start of the current instruction
     *               where the label reference occurs.  During backpatching,
     *               the label address is inserted at this offset.
     * @return A {@link LabelEntry} that represents the parsed label.
     * @throws SyntaxError The next symbol in the input file is not a valid
     *                     label reference.
     * @throws IOException An I/O error occured.
     */
    protected LabelEntry parseLabelReference(int offset)
        throws SyntaxError, IOException {

        String ident = getLabel();
        LabelEntry label = labels.get(ident);

        if (label == null) {
            label = new LabelEntry(ident, (short) 0, false);
            labels.put(ident, label);
        }

        label.backpatchInfos.add(new BackpatchInfo(pc, offset));

        return label;
    }

    /**
     * Tests if a token is an operand value.  Operand values can be variables,
     * labels, constants, and numbers.
     *
     * @param token The token to test.
     * @return True, if the token is an operand value; false, otherwise.
     */
    protected boolean isOperandValue(Lexer.Token token) {

        return token == Lexer.Token.VARIABLE
            || token == Lexer.Token.LABEL
            || token == Lexer.Token.CONSTANT
            || token == Lexer.Token.NUMBER;
    }

    /**
     * Tests if a token is a binary operator.
     *
     * @param token The token to test.
     * @return True, if the token is a binary operator; false, otherwise.
     */
    protected boolean isBinaryOperator(Lexer.Token token) {

        return token == Lexer.Token.OR
            || token == Lexer.Token.AND
            || token == Lexer.Token.XOR
            || token == Lexer.Token.PLUS
            || token == Lexer.Token.MINUS
            || token == Lexer.Token.MULT
            || token == Lexer.Token.DIV
            || token == Lexer.Token.EQUAL
            || token == Lexer.Token.NOT_EQUAL
            || token == Lexer.Token.GREATER
            || token == Lexer.Token.GREATER_EQUAL
            || token == Lexer.Token.LESS
            || token == Lexer.Token.LESS_EQUAL;
    }

    /**
     * Parses an operand value. Operand values can be variables, labels,
     * constants, and numbers.
     *
     * @param offset The byte offset from the start of the current instruction.
     * @throws SyntaxError The next symbol in the input file is not a valid
     *                     operand value.
     * @throws IOException An I/O error occured.
     */
    protected void parseOperandValue(int offset)
        throws SyntaxError, IOException {

        short constFlag = (offset == AntsVm.OP1_OFFSET) ?
            AntsVm.OP1_CONSTANT:
            AntsVm.OP2_CONSTANT;

        if (lexer.token == Lexer.Token.VARIABLE) {
            VariableEntry var = parseVariableReference();
            program[AntsVm.INSTRUCTION_SIZE * pc + AntsVm.OPCODE_OFFSET] &= ~constFlag;
            program[AntsVm.INSTRUCTION_SIZE * pc + offset] = var.address;

        } else if (lexer.token == Lexer.Token.LABEL) {
            LabelEntry label = parseLabelReference(offset);
            program[AntsVm.INSTRUCTION_SIZE * pc + AntsVm.OPCODE_OFFSET] |= constFlag;
            program[AntsVm.INSTRUCTION_SIZE * pc + offset] = 0;

        } else if (lexer.token == Lexer.Token.CONSTANT) {
            short val = getConstant();
            program[AntsVm.INSTRUCTION_SIZE * pc + AntsVm.OPCODE_OFFSET] |= constFlag;
            program[AntsVm.INSTRUCTION_SIZE * pc + offset] = val;

        } else if (lexer.token == Lexer.Token.NUMBER) {
            short val = getNumber();
            program[AntsVm.INSTRUCTION_SIZE * pc + AntsVm.OPCODE_OFFSET] |= constFlag;
            program[AntsVm.INSTRUCTION_SIZE * pc + offset] = val;

        } else {
            throw new SyntaxError(lexer,
                    "Variable, label, constant, or number expected.");
        }
    }

    /**
     * Parses the result value of an operation.  Only variables can be result
     * values.
     *
     * @throws SyntaxError The next symbol in the input file is not a valid
     *                     result value, i.e. is not a variable.
     * @throws IOException An I/O error occured.
     */
    protected void parseResultValue() throws SyntaxError, IOException {

        if (lexer.token == Lexer.Token.VARIABLE) {
            VariableEntry var = parseVariableReference();
            program[AntsVm.INSTRUCTION_SIZE * pc + AntsVm.RESULT_OFFSET] = var.address;
        } else {
            throw new SyntaxError(lexer, "Variable expected.");
        }
    }

    /**
     * Parses a binary operator.
     *
     * @throws SyntaxError The next symbol in the input file is not a valid
     *                     binary operator.
     * @throws IOException An I/O error occured.
     */
    protected void parseBinaryOperator() throws SyntaxError, IOException {

        String instr;

        switch (lexer.token) {
            case OR:            instr = "Or"; break;
            case AND:           instr = "And"; break;
            case XOR:           instr = "Xor"; break;
            case PLUS:          instr = "Add"; break;
            case MINUS:         instr = "Sub"; break;
            case MULT:          instr = "Mult"; break;
            case DIV:           instr = "Div"; break;
            case EQUAL:         instr = "Equal"; break;
            case NOT_EQUAL:     instr = "NotEqual"; break;
            case GREATER:       instr = "Greater"; break;
            case GREATER_EQUAL: instr = "GreaterEqual"; break;
            case LESS:          instr = "Less"; break;
            case LESS_EQUAL:    instr = "LessEqual"; break;
            default:
                throw new SyntaxError(lexer, "Binary operator expected.");
        }

        short opcode = (instructionMap.get(instr)).shortValue();
        program[AntsVm.INSTRUCTION_SIZE * pc + AntsVm.OPCODE_OFFSET] |= opcode;

        lexer.getNextToken();
    }

    /**
     * Parses a unary operator.
     *
     * @throws SyntaxError The next symbol in the input file is not a valid
     *                     unary operator.
     * @throws IOException An I/O error occured.
     */
    protected void parseUnaryOperator() throws SyntaxError, IOException {

        String instr;

        switch (lexer.token) {
            case NOT:   instr = "Not"; break;
            case MINUS: instr = "Neg"; break;
            default:
                throw new SyntaxError(lexer, "Unary operator expected.");
        }

        short opcode = (instructionMap.get(instr)).shortValue();
        program[AntsVm.INSTRUCTION_SIZE * pc + AntsVm.OPCODE_OFFSET] |= opcode;

        lexer.getNextToken();
    }

    /**
     * Parses a functional instruction.
     *
     * @throws SyntaxError The next symbols in the input file do not form
     *                     a valid functional instruction.
     * @throws IOException An I/O error occured.
     */
    protected void parseFunctionalInstruction()
        throws SyntaxError, IOException {

        short instr = getInstruction();
        program[AntsVm.INSTRUCTION_SIZE * pc + AntsVm.OPCODE_OFFSET] = instr;

        getToken(Lexer.Token.LPARENT);

        switch (instr) {

            case Instruction.MAKE_ANT:
            case Instruction.CLEAR_MARK:
            case Instruction.GOTO:
                parseOperandValue(AntsVm.OP1_OFFSET);
                break;

            case Instruction.STONES:
            case Instruction.OBSTACLES:
            case Instruction.FOOD:
            case Instruction.FOOD_AMOUNT:
            case Instruction.STONE_NUMBER:
            case Instruction.MOVE:
            case Instruction.COPY:
            case Instruction.NOT:
            case Instruction.BITS_TRUE:
            case Instruction.BITS_FALSE:
            case Instruction.NEG:
                parseOperandValue(AntsVm.OP1_OFFSET);
                getToken(Lexer.Token.COMMA);
                parseResultValue();
                break;

            case Instruction.ANTS:
            case Instruction.MARKS:
            case Instruction.MARK_VALUE:
            case Instruction.GET_FOOD:
            case Instruction.PUT_FOOD:
            case Instruction.GET_STONES:
            case Instruction.PUT_STONES:
            case Instruction.OR:
            case Instruction.AND:
            case Instruction.XOR:
            case Instruction.ADD:
            case Instruction.SUB:
            case Instruction.MULT:
            case Instruction.DIV:
            case Instruction.EQUAL:
            case Instruction.NOT_EQUAL:
            case Instruction.LESS:
            case Instruction.LESS_EQUAL:
            case Instruction.GREATER:
            case Instruction.GREATER_EQUAL:
                parseOperandValue(AntsVm.OP1_OFFSET);
                getToken(Lexer.Token.COMMA);
                parseOperandValue(AntsVm.OP2_OFFSET);
                getToken(Lexer.Token.COMMA);
                parseResultValue();
                break;

            case Instruction.SET_MARK:
            case Instruction.GOTO_IF:
                parseOperandValue(AntsVm.OP1_OFFSET);
                getToken(Lexer.Token.COMMA);
                parseOperandValue(AntsVm.OP2_OFFSET);
                break;

            default:
                throw new SyntaxError(lexer,
                        "Invalid instruction code: " + instr + ".");
        }

        getToken(Lexer.Token.RPARENT);

        pc++;

        if (pc > AntsVm.MAX_PROGRAM_SIZE) {
            throw new SyntaxError(lexer, "Program must not be longer than " +
                    AntsVm.MAX_PROGRAM_SIZE + " instructions.");
        }
    }

    /**
     * Parses an assignment instruction.
     *
     * @throws SyntaxError The next symbols in the input file do not form
     *                     a valid assignment instruction.
     * @throws IOException An I/O error occured.
     */
    protected void parseAssignInstruction() throws SyntaxError, IOException {

        parseResultValue();
        getToken(Lexer.Token.ASSIGN);

        if (isOperandValue(lexer.token)) {
            parseOperandValue(AntsVm.OP1_OFFSET);
            if (isBinaryOperator(lexer.token)) {
                parseBinaryOperator();
                parseOperandValue(AntsVm.OP2_OFFSET);
            } else {
                short opcode = (instructionMap.get("Copy")).shortValue();
                program[AntsVm.INSTRUCTION_SIZE * pc + AntsVm.OPCODE_OFFSET] |= opcode;
            }

        } else if (lexer.token == Lexer.Token.NOT || lexer.token == Lexer.Token.MINUS) {
            parseUnaryOperator();
            parseOperandValue(AntsVm.OP1_OFFSET);

        } else {
            throw new SyntaxError(lexer,
                    "Invalid expression on right side of assignment.");
        }

        pc++;

        if (pc > AntsVm.MAX_PROGRAM_SIZE) {
            throw new SyntaxError(lexer, "Program must not be longer than " +
                    AntsVm.MAX_PROGRAM_SIZE + " instructions.");
        }
    }

    /**
     * Parses a label definition.
     *
     * @throws SyntaxError The next symbols in the input file do not form
     *                     a valid label definition.
     * @throws IOException An I/O error occured.
     */
    protected void parseLabelDefinition() throws SyntaxError, IOException {

        String ident = getLabel();
        getToken(Lexer.Token.COLON);

        if (labels.containsKey(ident)) {

            LabelEntry label = labels.get(ident);

            if (label.defined) {
                throw new SyntaxError(lexer, "Label redefined.");
            } else {
                label.address = pc;
                label.defined = true;
            }

        } else {
            LabelEntry label = new LabelEntry(ident, pc, true);
            labels.put(ident, label);
        }
    }

    /**
     * Performs the backpatching step.  During backpatching, the addresses of
     * all labels are inserted at the respective label references.
     */
    protected void backpatchLabels() {

        for (LabelEntry label: labels.values()) {
            for (BackpatchInfo bpi: label.backpatchInfos) {
                program[AntsVm.INSTRUCTION_SIZE * bpi.instruction + bpi.offset] =
                    label.address;
            }
        }
    }

    /**
     * The main method of the ant compiler.
     *
     * @param args An array of paths of the source files to be compiled.
     */
    public static void main(String[] args) {

        if (args.length < 1) {
            System.out.println(
                "Usage: java ants.compiler.AntsCompiler [source files ...]");
        } else {

            for (int i = 0; i < args.length; i++) {

                System.out.println("Compiling " + args[i]);

                AntClass ant;

                try (FileReader srcReader = new FileReader(args[i])) {

                    AntsCompiler compiler = new AntsCompiler();
                    ant = compiler.compile(srcReader);

                } catch (SyntaxError e) {
                    System.out.println(
                            "Syntax error (line " + e.getLine() + "): " +
                            e.getMessage() + ".");
                    continue;

                } catch (IOException e) {
                    System.out.println(
                            "Error while opening or reading input file: " +
                            e.getMessage() + ".");
                    continue;
                }

                try (FileOutputStream fos = new FileOutputStream(args[i] + ".bin");
                     ObjectOutputStream oos = new ObjectOutputStream(fos)) {

                    oos.writeObject(ant);

                } catch (IOException e) {
                    System.out.println(
                            "Error while writing output file: " +
                            e.getMessage() + ".");
                    continue;
                }
            }
        }
    }

    /**
     * Compiles an ant source file.
     *
     * @param r The character stream providing the ant source.
     * @return The {@link AntClass} that has been compiled from the
     *         source file.
     * @throws SyntaxError The source file did not contain a valid description
     *                     of an ant class.
     * @throws IOException An I/O error occured.
     */
    public AntClass compile(Reader r) throws SyntaxError, IOException {

        lexer = new Lexer(r);

        variables = new HashMap<>();
        labels = new HashMap<>();

        for (short i = 0; i < defaultVariables.length; i++)
            variables.put(defaultVariables[i],
                new VariableEntry(defaultVariables[i], i));

        pc = 0;
        vc = (short) variables.size();

        ant = new AntClass();
        program = new short[AntsVm.INSTRUCTION_SIZE * AntsVm.MAX_PROGRAM_SIZE];

        parseAnt();
        backpatchLabels();

        short[] antProgram = Arrays.copyOf(program, AntsVm.INSTRUCTION_SIZE * pc);

        ant.setVariableSize((short) variables.size());
        ant.setProgramSize(pc);
        ant.setProgram(antProgram);

        return ant;
    }
}
