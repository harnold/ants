package ants.compiler;

/**
 * The <code>SyntaxError</code> exception is thrown if a syntax error occurs
 * in a run of the Ants compiler.
 */
public class SyntaxError extends Exception {

    private int line;

    /**
     * Creates a new <code>SyntaxError</code> exception.
     *
     * @param line The source code line where the error is located.
     * @param msg A message that describes the error.
     */
    public SyntaxError(Lexer lexer, String msg) {
        super(msg);
        this.line = lexer.getLineNumber();
    }

    /**
     * Returns the source code line where the error is located.
     */
    public int getLine() {
        return line;
    }
}
