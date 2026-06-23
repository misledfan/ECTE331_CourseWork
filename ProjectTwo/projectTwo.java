/**
 * Utility class providing the integer summation method.
 * All members are static; this class is not instantiable.
 *
 * @author Safwaan Syed
 * @version 1.0
 */

class MathUtils {

    /** Private constructor – utility class, not for instantiation. */
    private MathUtils() {
    }

    /**
     * Summation Loop
     * Calculates of integers from 0 to n using a loop.
     *
     * @param n upper limit (inclusive)
     * @return sum 0 + 1 + 2 + ... + n
     */
    static long sumTo(int n) {
        long total = 0;
        for (int i = 0; i <= n; i++)
            total += i; // accumulate each term
        return total;
    }
}

// ── Shared Variables
// ──────────────────────────────────────────────────────────

/**
 * Holds the six shared integer variables updated by ThreadA and ThreadB.
 *
 * @author Safwaan Syed
 * @version 1.0
 */
class SharedVars {
    /** Result of FuncA1 = sum(0..500). */
    static long A1;
    /** Result of FuncA2 = B2 + sum(0..300). */
    static long A2;
    /** Result of FuncA3 = B3 + sum(0..400). */
    static long A3;
    /** Result of FuncB1 = sum(0..250). */
    static long B1;
    /** Result of FuncB2 = A1 + sum(0..200). */
    static long B2;
    /** Result of FuncB3 = A2 + sum(0..400). */
    static long B3;
}

// ── Monitor (shared lock + condition flags)
// ───────────────────────────────────

/**
 * Shared monitor object used as the lock for
 * {@code wait()}/{@code notifyAll()}.
 *
 * @author Safwaan Syed
 * @version 1.0
 */
class Monitor {
    /** Set to true when FuncA1 has written A1. Unblocks FuncB2. */
    boolean a1Done = false;
    /** Set to true when FuncB2 has written B2. Unblocks FuncA2. */
    boolean b2Done = false;
    /** Set to true when FuncA2 has written A2. Unblocks FuncB3. */
    boolean a2Done = false;
    /** Set to true when FuncB3 has written B3. Unblocks FuncA3. */
    boolean b3Done = false;
}

// ── Thread A
// ──────────────────────────────────────────────────────────────────

/**
 * Thread A executes FuncA1 → FuncA2 → FuncA3 in sequence.
 *
 * <p>
 * Synchronisation via {@code wait()}/{@code notifyAll()} on a shared
 * {@link Monitor}:
 * <ul>
 * <li>FuncA1 sets {@code a1Done} and notifies all.</li>
 * <li>FuncA2 waits while {@code !b2Done}, then sets {@code a2Done}.</li>
 * <li>FuncA3 waits while {@code !b3Done}.</li>
 * </ul>
 * No active waiting or {@code Thread.sleep} is used.
 * </p>
 *
 * @author Safwaan Syed
 * @version 1.0
 */
class ThreadA extends Thread {

    /** Shared monitor – lock object and condition flags. */
    private final Monitor mon;

    /**
     * Constructs ThreadA with the shared monitor.
     *
     * @param mon shared {@link Monitor} instance
     */
    ThreadA(Monitor mon) {
        this.mon = mon;
    }

    /**
     * FuncA1: computes A1 = sum(0..500), signals that A1 is ready.
     */
    private void funcA1() {
        SharedVars.A1 = MathUtils.sumTo(500);
        synchronized (mon) {
            mon.a1Done = true; // mark A1 complete
            mon.notifyAll(); // wake any thread waiting on a1Done
        }
    }

    /**
     * FuncA2: waits for B2, then computes A2 = B2 + sum(0..300), signals A2 ready.
     *
     * @throws InterruptedException if interrupted while waiting
     */
    private void funcA2() throws InterruptedException {
        synchronized (mon) {
            while (!mon.b2Done)
                mon.wait(); // block until B2 is written
        }
        SharedVars.A2 = SharedVars.B2 + MathUtils.sumTo(300);
        synchronized (mon) {
            mon.a2Done = true; // mark A2 complete
            mon.notifyAll(); // wake FuncB3
        }
    }

    /**
     * FuncA3: waits for B3, then computes A3 = B3 + sum(0..400).
     *
     * @throws InterruptedException if interrupted while waiting
     */
    private void funcA3() throws InterruptedException {
        synchronized (mon) {
            while (!mon.b3Done)
                mon.wait(); // block until B3 is written
        }
        SharedVars.A3 = SharedVars.B3 + MathUtils.sumTo(400);
    }

    /**
     * Runs FuncA1, FuncA2, FuncA3 sequentially with blocking synchronisation.
     */
    @Override
    public void run() {
        try {
            funcA1();
            funcA2();
            funcA3();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // restore interrupt flag
        }
    }
}

// ── Thread B
// ──────────────────────────────────────────────────────────────────

/**
 * Thread B executes FuncB1 → FuncB2 → FuncB3 in sequence.
 *
 * <p>
 * Synchronisation via {@code wait()}/{@code notifyAll()} on a shared
 * {@link Monitor}:
 * <ul>
 * <li>FuncB1 runs freely (no dependency).</li>
 * <li>FuncB2 waits while {@code !a1Done}, then sets {@code b2Done}.</li>
 * <li>FuncB3 waits while {@code !a2Done}, then sets {@code b3Done}.</li>
 * </ul>
 * </p>
 *
 * @author Safwaan Syed
 * @version 1.0
 */
class ThreadB extends Thread {

    /** Shared monitor – lock object and condition flags. */
    private final Monitor mon;

    /**
     * Constructs ThreadB with the shared monitor.
     *
     * @param mon shared {@link Monitor} instance
     */
    ThreadB(Monitor mon) {
        this.mon = mon;
    }

    /**
     * FuncB1: computes B1 = sum(0..250). No synchronisation needed;
     * B1 does not depend on any variable from Thread A.
     */
    private void funcB1() {
        SharedVars.B1 = MathUtils.sumTo(250); // independent of Thread A
    }

    /**
     * FuncB2: waits for A1, then computes B2 = A1 + sum(0..200), signals B2 ready.
     *
     * @throws InterruptedException if interrupted while waiting
     */
    private void funcB2() throws InterruptedException {
        synchronized (mon) {
            while (!mon.a1Done)
                mon.wait(); // block until A1 is written
        }
        SharedVars.B2 = SharedVars.A1 + MathUtils.sumTo(200);
        synchronized (mon) {
            mon.b2Done = true; // mark B2 complete
            mon.notifyAll(); // wake FuncA2
        }
    }

    /**
     * FuncB3: waits for A2, then computes B3 = A2 + sum(0..400), signals B3 ready.
     *
     * @throws InterruptedException if interrupted while waiting
     */
    private void funcB3() throws InterruptedException {
        synchronized (mon) {
            while (!mon.a2Done)
                mon.wait(); // block until A2 is written
        }
        SharedVars.B3 = SharedVars.A2 + MathUtils.sumTo(400);
        synchronized (mon) {
            mon.b3Done = true; // mark B3 complete
            mon.notifyAll(); // wake FuncA3
        }
    }

    /**
     * Runs FuncB1, FuncB2, FuncB3 sequentially with blocking synchronisation.
     */
    @Override
    public void run() {
        try {
            funcB1();
            funcB2();
            funcB3();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // restore interrupt flag
        }
    }
}

// ── Main Class
// ────────────────────────────────────────────────────────────────

/**
 * ECTE331 Part 2 – Thread Synchronisation and Communication.
 *
 * <p>
 * Implements the execution using Java's built-inmonitor mechanism
 * ({@code synchronized} / {@code wait()} / {@code notifyAll()})
 * No active waiting or {@code Thread.sleep} is used; blocked threads yield the
 * CPU to the OS scheduler.
 * </p>
 *
 * <p>
 * A {@code while(!flag) wait()} pattern guards every condition check to
 * handle spurious wakeups correctly. {@code notifyAll()} is preferred over
 * {@code notify()} to ensure all blocked threads re-evaluate their condition.
 * </p>
 *
 * <p>
 * Part (d): {@code main} runs {@value #ITERATIONS} iterations to verify
 * correctness irrespective of OS scheduling order.
 * </p>
 *
 * <p>
 * Compile and run:
 * </p>
 * 
 * <pre>
 *   javac projectTwo.java
 *   java  projectTwo
 * </pre>
 *
 * @author Safwaan Syed
 * @version 1.0
 */
public class projectTwo {

    // ── Expected Correct Values (Part a) ──────────────────────────────────────

    /** Expected A1 = sum(0..500) = 500*501/2 = 125,250. */
    static final long EXP_A1 = 125_250L;
    /** Expected B1 = sum(0..250) = 250*251/2 = 31,375. */
    static final long EXP_B1 = 31_375L;
    /** Expected B2 = A1 + sum(0..200) = 125250 + 20100 = 145,350. */
    static final long EXP_B2 = 145_350L;
    /** Expected A2 = B2 + sum(0..300) = 145350 + 45150 = 190,500. */
    static final long EXP_A2 = 190_500L;
    /** Expected B3 = A2 + sum(0..400) = 190500 + 80200 = 270,700. */
    static final long EXP_B3 = 270_700L;
    /** Expected A3 = B3 + sum(0..400) = 270700 + 80200 = 350,900. */
    static final long EXP_A3 = 350_900L;

    /** Number of iterations for the stress test (Part d). */
    static final int ITERATIONS = 1000;

    /**
     * Runs one iteration: creates a fresh {@link Monitor}, starts both threads,
     * joins them, and validates all shared variables.
     *
     * <p>
     * A new {@code Monitor} per iteration resets all boolean flags to
     * {@code false} so state cannot leak between runs.
     * </p>
     *
     * @param iter iteration index used in error messages
     * @return {@code true} if all six variables match expected values
     * @throws InterruptedException if the main thread is interrupted during join
     */
    static boolean runIteration(int iter) throws InterruptedException {
        Monitor mon = new Monitor(); // fresh flags for this iteration

        ThreadA tA = new ThreadA(mon);
        ThreadB tB = new ThreadB(mon);

        tA.start(); // start both threads – they race on FuncA1 and FuncB1
        tB.start();
        tA.join(); // wait for Thread A to finish all three functions
        tB.join(); // wait for Thread B to finish all three functions

        // check all six results
        boolean pass = SharedVars.A1 == EXP_A1 && SharedVars.A2 == EXP_A2 &&
                SharedVars.A3 == EXP_A3 && SharedVars.B1 == EXP_B1 &&
                SharedVars.B2 == EXP_B2 && SharedVars.B3 == EXP_B3;

        if (!pass) // print details on any mismatch
            System.out.printf("  MISMATCH iter %d: A1=%d B1=%d B2=%d A2=%d B3=%d A3=%d%n",
                    iter, SharedVars.A1, SharedVars.B1, SharedVars.B2,
                    SharedVars.A2, SharedVars.B3, SharedVars.A3);
        return pass;
    }

    /**
     * Entry point. Prints expected values, performs a single traced run,
     * then stress-tests for {@value #ITERATIONS} iterations (Part d).
     *
     * @param args command-line arguments (not used)
     * @throws InterruptedException if the main thread is interrupted
     */
    public static void main(String[] args) throws InterruptedException {
        System.out.println("Thread Synchronisation -ECTE331 Project II ");
        System.out.printf("Expected: \n A1=%d   B1=%d  \n B2=%d   A2=%d  \n B3=%d   A3=%d%n",
                EXP_A1, EXP_B1, EXP_B2, EXP_A2, EXP_B3, EXP_A3);

        // ── Single traced run ────────────────────────────────────────────────
        runIteration(0);
        System.out.println("\nSingle-run results:");
        System.out.printf("  A1=%-8d  B1=%-8d%n", SharedVars.A1, SharedVars.B1);
        System.out.printf("  B2=%-8d  A2=%-8d%n", SharedVars.B2, SharedVars.A2);
        System.out.printf("  B3=%-8d  A3=%-8d%n", SharedVars.B3, SharedVars.A3);

        // ── Stress test (Part d) ─────────────────────────────────────────────
        // Running many iterations proves correctness regardless of OS scheduling.
        // Without synchronisation, race conditions would produce random mismatches.
        System.out.println("\nRunning " + ITERATIONS + " iterations...");
        int failures = 0;
        for (int i = 1; i <= ITERATIONS; i++) {
            if (!runIteration(i))
                failures++;
        }

        if (failures == 0)
            System.out.println("All " + ITERATIONS + " iterations PASSED – synchronisation is correct.");
        else
            System.out.println(failures + " / " + ITERATIONS + " iterations FAILED.");
    }

} // class projectTwo