package ants.test;

import java.util.*;

/**
 * The <code>TestSuite</code> class implements a collection of tests.  A
 * single run of a test suite causes all contained tests to be run.
 */
public class TestSuite extends Test {

    ArrayList<Test> tests;

    /**
     * Creates a new <code>TestSuite</code>.
     */
    public TestSuite() {
        tests = new ArrayList<>();
    }

    /**
     * Runs the test suite.  This causes all containes tests to be run.
     *
     * @throws TestFailedException A test in the test suite failed.
     */
    public void run() throws TestFailedException {
        for (Test t: tests)
            t.run();
    }

    /**
     * Adds a test to the test suite.
     *
     * @param t The test to add.
     */
    public void addTest(Test t) {
        tests.add(t);
    }
}
