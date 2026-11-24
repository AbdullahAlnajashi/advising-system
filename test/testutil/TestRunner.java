package testutil;

import java.util.ArrayList;

/**
 * Minimal, dependency-free test runner so the project builds with nothing but a JDK.
 * Each test is a named lambda; a test fails if it throws.
 */
public final class TestRunner {

    @FunctionalInterface
    public interface TestBody {
        void run() throws Exception;
    }

    private static final class Case {
        final String name;
        final TestBody body;

        Case(String name, TestBody body) {
            this.name = name;
            this.body = body;
        }
    }

    private final ArrayList<Case> cases = new ArrayList<>();

    public void test(String name, TestBody body) {
        cases.add(new Case(name, body));
    }

    /** Runs every registered test and returns the number of failures. */
    public int runAll(String suiteName) {
        int failed = 0;
        System.out.println("== " + suiteName);
        for (Case c : cases) {
            try {
                c.body.run();
                System.out.println("  PASS  " + c.name);
            } catch (Throwable t) {
                failed++;
                System.out.println("  FAIL  " + c.name + "\n        -> " + t);
            }
        }
        return failed;
    }

    // ---- assertions ----

    public static void assertTrue(boolean condition, String message) {
        if (!condition)
            throw new AssertionError(message);
    }

    public static void assertFalse(boolean condition, String message) {
        assertTrue(!condition, message);
    }

    public static void assertEquals(Object expected, Object actual, String message) {
        if (expected == null ? actual != null : !expected.equals(actual))
            throw new AssertionError(message + " (expected <" + expected + "> but was <" + actual + ">)");
    }

    public static <E extends Throwable> E assertThrows(Class<E> type, TestBody body, String message) {
        try {
            body.run();
        } catch (Throwable t) {
            if (type.isInstance(t))
                return type.cast(t);
            throw new AssertionError(message + " (expected " + type.getSimpleName() + " but got " + t + ")");
        }
        throw new AssertionError(message + " (expected " + type.getSimpleName() + " but nothing was thrown)");
    }
}
