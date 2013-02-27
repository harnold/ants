package ants.test;

/**
 * A <code>TestFailedException</code> is thrown whenever a test fails.
 */
public class TestFailedException extends Exception {

    Test test;

    /**
     * Creates a new <code>TestFailedException</code>.
     *
     * @param test The test that has failed.
     */
    public TestFailedException(Test test) {
        super("");
        this.test = test;
    }

    /**
     * Creates a new <code>TestFailedException</code>.
     *
     * @param test The test that has failed.
     * @param message An informational message.
     */
    public TestFailedException(Test test, String message) {
        super(message);
        this.test = test;
    }

    /**
     * Returns the test that has failed.
     *
     * @return The test that has failed.
     */
    public Test getTest() {
        return test;
    }
}
