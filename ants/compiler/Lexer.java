package ants.compiler;

import java.io.*;

/**
 * The <code>Lexer</code> class implements the lexical analyzer of the Ants
 * compiler.  This component translates character streams to streams of
 * lexical symbols.  To use this class, create a <code>Lexer</code> object
 * with the character stream to be analyzed and call {@link #getNextToken()}
 * until <code>token == Lexer.Token.EOF</code>.  {@link #token} always
 * contains the last token read from the stream and {@link #stringValue} or
 * {@link #numValue} its value, if needed.
 */
public class Lexer {

    public static enum Token {
        NOTHING, EOF, OR, AND, XOR, PLUS, MINUS, MULT, DIV, NOT, EQUAL,
        NOT_EQUAL, LESS, LESS_EQUAL, GREATER, GREATER_EQUAL, VARIABLE,
        CONSTANT, LABEL, NUMBER, IDENTIFIER, DEFINE_ANT, CONFIGURATION,
        PROGRAM, LPARENT, RPARENT, COLON, COMMA, ASSIGN
    };

    /**
     * The character stream to read from.
     */
    protected LineNumberReader reader;

    /**
     * The last character read from the character stream.
     */
    protected char c;

    /**
     * Reads an identifier from the character stream and stores its value to
     * <code>stringValue</code>.
     *
     * @throws IOException An I/O error occured.
     */
    protected void readIdentifier() throws IOException {

        StringBuffer buf = new StringBuffer();

        while (Character.isLetterOrDigit(c) || c == '_') {
            buf.append(c);
            read();
        }

        stringValue = buf.toString();
    }

    /**
     * Reads a number from the character stream and stores its value in
     * <code>numValue</code>.
     *
     * @throws IOException An I/O error occured.
     */
    protected void readNumber() throws IOException {

        numValue = 0;

        while (Character.isDigit(c)) {
            numValue = (short) (10 * numValue + Character.digit(c, 10));
            read();
        }
    }

    /**
     * Reads the next character from the character stream and stores its value
     * in <code>c</code>.
     *
     * @throws IOException An I/O error occured.
     */
    protected void read() throws IOException {
        c = (char) reader.read();
    }

    /**
     * The last symbol that has been analyzed.
     */
    public Token token = Token.NOTHING;

    /**
     * The value of the current token if it has a string type.  This value is
     * valid if <code>token</code> is <code>VARIABLE</code>,
     * <code>CONSTANT</code>, <code>LABEL</code>, or <code>IDENTIFIER</code>.
     */
    public String stringValue;

    /**
     * The value of the current token if it has a numeric type.  This value is
     * valid if <code>token</code> is <code>NUMBER</code>.
     */
    public short numValue;

    /**
     * Creates a new <code>Lexer</code> object that analyzes a given character
     * stream.
     *
     * @param r The reader that provides the character stream to be analyzed.
     * @throws IOException An I/O error occured during the first read.
     */
    public Lexer(Reader r) throws IOException {
        reader = new LineNumberReader(r);
        read();
    }

    /**
     * Returns the line number of the current read position.
     *
     * @return The current line number.
     */
    public int getLineNumber() {
        return reader.getLineNumber();
    }

    /**
     * Reads the next token from the character stream.
     *
     * @return The next token.
     * @throws SyntaxError A syntax error occured while reading the token.
     * @throws IOException An I/O error occured while reading the token.
     */
    public Token getNextToken() throws SyntaxError, IOException {

        while (Character.isWhitespace(c) || c == ';') {

            while (Character.isWhitespace(c))
                read();

            if (c == ';') {
                while (c != '\n' && c != (char) -1)
                    read();
            }
        }

        token = Token.NOTHING;
        numValue = 0;
        stringValue = "";

        if (c == (char) -1) {
            token = Token.EOF;
        } else if (c == '(') {
            token = Token.LPARENT;
            read();
        } else if (c == ')') {
            token = Token.RPARENT;
            read();
        } else if (c == ':') {
            token = Token.COLON;
            read();
        } else if (c == '|') {
            token = Token.OR;
            read();
        } else if (c == '&') {
            token = Token.AND;
            read();
        } else if (c == '^') {
            token = Token.XOR;
            read();
        } else if (c == ',') {
            token = Token.COMMA;
            read();
        } else if (c == '+') {
            token = Token.PLUS;
            read();
        } else if (c == '-') {
            token = Token.MINUS;
            read();
        } else if (c == '*') {
            token = Token.MULT;
            read();
        } else if (c == '/') {
            token = Token.DIV;
            read();

        } else if (c == '=') {
            read();
            if (c == '=') {
                read();
                token = Token.EQUAL;
            } else {
                token = Token.ASSIGN;
            }

        } else if (c == '!') {
            read();
            if (c == '=') {
                read();
                token = Token.NOT_EQUAL;
            } else {
                token = Token.NOT;
            }

        } else if (c == '<') {
            read();
            if (c == '=') {
                read();
                token = Token.LESS_EQUAL;
            } else {
                token = Token.LESS;
            }

        } else if (c == '>') {
            read();
            if (c == '=') {
                read();
                token = Token.GREATER_EQUAL;
            } else {
                token = Token.GREATER;
            }

        } else if (c == '$') {

            read();

            if (!Character.isLetter(c)) {
                throw new SyntaxError(this,
                        "Variable must start with a letter.");
            } else {
                readIdentifier();
                token = Token.VARIABLE;
            }

        } else if (c == '#') {

            read();

            if (!Character.isLetter(c)) {
                throw new SyntaxError(this,
                        "Constant must start with a letter.");
            } else {
                readIdentifier();
                token = Token.CONSTANT;
            }

        } else if (c == '%') {

            read();

            if (!Character.isLetter(c)) {
                throw new SyntaxError(this,
                       "Label must start with a letter.");
            } else {
                readIdentifier();
                token = Token.LABEL;
            }

        } else if (Character.isDigit(c)) {
            readNumber();
            token = Token.NUMBER;

        } else if (Character.isLetter(c)) {

            readIdentifier();

            if (stringValue.equals("DefineAnt")) {
                token = Token.DEFINE_ANT;
            } else if (stringValue.equals("Configuration")) {
                token = Token.CONFIGURATION;
            } else if (stringValue.equals("Program")) {
                token = Token.PROGRAM;
            } else {
                token = Token.IDENTIFIER;
            }

        } else {
            throw new SyntaxError(this,
                    "Unexpected character '" + c + "'.");
        }

        return token;
    }
}
