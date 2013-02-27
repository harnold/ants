package ants.test;

/**
 * The <code>Test</code> class is an abstract base class for unit tests or
 * sets of unit tests.  Unit tests classes should be derived from this class
 * and should override the {@link #run()} method, where the test should be
 * implemented.  The {@link #assertCond(boolean, String)} method can be used
 * to validate conditions and eventually cause the test to fail.
 */
public abstract class Test {

    /**
     * Runs the test.  Derived classes should override this method.
     *
     * @throws TestFailedException The test failed as a result of a failed
     *                             assertion or an explicit fail.
     */
    public abstract void run() throws TestFailedException;

    /**
     * Asserts a boolean condition.
     *
     * @param condition The asserted condition.
     * @throws TestFailedException The condition has been evaluated to
     *                             <code>false</code>.
     */
    protected void assertCond(boolean condition) throws TestFailedException {
        if (!condition)
            fail();
    }

    /**
     * Asserts a boolean condition.
     *
     * @param condition The asserted condition.
     * @param message An informational message.
     * @throws TestFailedException The condition has been evaluated to
     *                             <code>false</code>.
     */
    protected void assertCond(boolean condition, String message)
        throws TestFailedException {
        if (!condition)
            fail(message);
    }

    /**
     * Causes the test to fail unconditionally, i.e., immediately throws
     * a {@link TestFailedException}.
     *
     * @throws TestFailedException Always.
     */
    protected void fail() throws TestFailedException {
        throw new TestFailedException(this);
    }

    /**
     * Causes the test to fail unconditionally, i.e., immediately throws
     * a {@link TestFailedException}.
     *
     * @param message An informational message.
     * @throws TestFailedException Always.
     */
    protected void fail(String message) throws TestFailedException {
        throw new TestFailedException(this, message);
    }
}
