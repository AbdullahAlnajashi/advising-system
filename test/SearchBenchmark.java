import java.util.Random;

/**
 * Compares student search cost in Phase 1 (sorted linked list, linear search) with
 * Phase 2 (binary search tree). Cost is measured in key comparisons, which is
 * deterministic, plus wall-clock time for searching every student once.
 *
 * Run: java -cp out SearchBenchmark
 */
public class SearchBenchmark {

    private static final int BASE_ID = 4_451_000;

    public static void main(String[] args) {
        System.out.println("| Students | Linked list: avg comparisons | BST (random order): avg comparisons"
                + " | Linked list: time | BST: time |");
        System.out.println("|---:|---:|---:|---:|---:|");
        for (int n : new int[]{100, 1_000, 10_000})
            run(n);

        int n = 1_000;
        Phase2.BSTSet<Integer> sorted = new Phase2.BSTSet<>();
        for (int i = 0; i < n; i++)
            sorted.insert(BASE_ID + i);
        long total = 0;
        for (int i = 0; i < n; i++)
            total += sorted.nbKeyComp(BASE_ID + i);
        System.out.printf(java.util.Locale.ROOT, "%nWorst case: BST built from %,d IDs inserted in sorted order -> %.1f avg comparisons%n",
                n, (double) total / n);
    }

    private static void run(int n) {
        int[] ids = shuffledIds(n, new Random(42));

        // Phase 1: students in a sorted singly linked list.
        advising.StudentList list = new advising.StudentList();
        for (int id : ids)
            list.add(new advising.Student("S" + id, id, id + "@example.com", "CS", 1, ""));

        // Phase 2: students in a binary search tree.
        Phase2.AdvisingSystemPhase2 system = new Phase2.AdvisingSystemPhase2();
        Phase2.BSTSet<Integer> keys = new Phase2.BSTSet<>();
        for (int id : ids) {
            system.addStudent(new Phase2.Student(id, "S" + id, id + "@example.com", new Phase2.Schedule(), "CS"));
            keys.insert(id);
        }

        // A linear search for the k-th smallest ID makes k comparisons; average = (n + 1) / 2.
        double listAvg = (n + 1) / 2.0;

        long bstTotal = 0;
        for (int id : ids)
            bstTotal += keys.nbKeyComp(id);
        double bstAvg = (double) bstTotal / n;

        long listNanos = time(() -> {
            for (int id : ids)
                if (list.findById(id) == null)
                    throw new AssertionError();
        });
        long bstNanos = time(() -> {
            for (int id : ids)
                if (system.searchStudentById(id) == null)
                    throw new AssertionError();
        });

        System.out.printf(java.util.Locale.ROOT, "| %,d | %,.1f | %.1f | %s | %s |%n", n, listAvg, bstAvg, ms(listNanos), ms(bstNanos));
    }

    private static int[] shuffledIds(int n, Random random) {
        int[] ids = new int[n];
        for (int i = 0; i < n; i++)
            ids[i] = BASE_ID + i;
        for (int i = n - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            int tmp = ids[i];
            ids[i] = ids[j];
            ids[j] = tmp;
        }
        return ids;
    }

    /** Best of 5 runs, after a warm-up, to reduce JIT and GC noise. */
    private static long time(Runnable work) {
        work.run();
        long best = Long.MAX_VALUE;
        for (int i = 0; i < 5; i++) {
            long start = System.nanoTime();
            work.run();
            best = Math.min(best, System.nanoTime() - start);
        }
        return best;
    }

    private static String ms(long nanos) {
        return String.format(java.util.Locale.ROOT, "%.2f ms", nanos / 1e6);
    }
}
