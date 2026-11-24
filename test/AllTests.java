/** Entry point that runs every test suite and exits non-zero on failure. */
public class AllTests {
    public static void main(String[] args) {
        int failures = advising.Phase1Tests.run() + Phase2.Phase2Tests.run();
        System.out.println(failures == 0 ? "\nALL TESTS PASSED" : "\n" + failures + " TEST(S) FAILED");
        System.exit(failures == 0 ? 0 : 1);
    }
}
