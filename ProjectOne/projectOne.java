import java.io.*;
import java.time.LocalDateTime;
import java.util.Random;

// ── Custom Exceptions ────────────────────────────────────────────────────────

/**
 * Thrown when a sensor experiences a hardware failure.
 * Extends {@link IOException} as sensor failure is treated as an I/O-level
 * fault.
 *
 * @author Safwaan Syed
 * @version 2.0
 */
class SensorReadException extends IOException {

    /**
     * Constructs a SensorReadException with the given detail message.
     *
     * @param msg description of the sensor that failed
     */
    public SensorReadException(String msg) {
        super(msg); // pass message up to IOException
    }
}

/**
 * Thrown when two consecutive reliability failures occur, triggering SAFE MODE.
 * Extends {@link Exception} as this is an application-level fault condition.
 *
 * @author ECTE331 Student
 * @version 1.0
 */
class SystemReliabilityException extends Exception {

    /**
     * Constructs a SystemReliabilityException with the given detail message.
     *
     * @param msg description of the reliability condition that was violated
     */
    public SystemReliabilityException(String msg) {
        super(msg); // pass message up to Exception
    }
}

/**
 * ECTE331 Part 1 – Fault-Tolerant Drone Navigation System.
 * Uses Triple Modular Redundancy (TMR) across 3 altitude sensors.
 * Majority vote decides altitude; 2 consecutive failures trigger SAFE MODE.
 *
 * @author ECTE331 Student
 * @version 1.0
 */
public class projectOne {

    /** Minimum valid altitude in metres (inclusive). */
    static final int MIN = 0;

    /** Maximum valid altitude in metres (inclusive). */
    static final int MAX = 200;

    /** Baseline altitude used to seed valid sensor readings (metres). */
    static final int BASELINE = 100;

    /** Total number of sense-vote cycles to run. */
    static final int CYCLES = 10;

    /** Shared random number generator for all fault simulation. */
    static Random rng = new Random();

    /** Auto-flushing writer for the timestamped log file. */
    static PrintWriter log;

    /** Last confirmed valid altitude; used as fallback when no majority found. */
    static int lastGood = BASELINE;

    /** Counter of back-to-back reliability failures; resets on success. */
    static int consecFails = 0;

    // ── Sensor Read: applies TMR fault model ─────────────────────────────────

    /**
     * Simulates a sensor reading with the TMR fault model.
     * chance 0–14 → SensorReadException (hardware failure)
     * chance 15–29 → corrupted value outside [0,200]
     * chance 30–99 → valid value in [0,200]
     *
     * @param id sensor label e.g. "A"
     * @return simulated altitude reading
     * @throws SensorReadException on hardware failure
     */
    static int readSensor(String id) throws SensorReadException {
        int chance = rng.nextInt(100); // roll 0–99
        if (chance < 15)
            throw new SensorReadException("Sensor " + id + " hardware failure");
        else if (chance < 30)
            return MAX + 1 + rng.nextInt(100); // corrupted: out-of-range (201–300)
        else
            return BASELINE + rng.nextInt(21) - 10; // valid: ~[90,110], clamped below
    }

    /**
     * Checks whether an altitude value is within the valid range [0, 200].
     *
     * @param v altitude value to validate
     * @return {@code true} if valid, {@code false} if out-of-range (corrupted)
     */
    static boolean isValid(int v) {
        return v >= MIN && v <= MAX; // inclusive bounds check
    }

    // ── Logging ───────────────────────────────────────────────────────────────

    /**
     * Writes a timestamped line to log.txt.
     * 
     * @param msg event description
     */
    static void log(String msg) {
        log.println("[" + LocalDateTime.now().toString().substring(0, 19) + "] " + msg);
    }

    // ── One TMR Cycle ─────────────────────────────────────────────────────────

    /**
     * Runs one sense-vote cycle: poll sensors, validate, majority vote, log.
     * Throws SystemReliabilityException on second consecutive failure.
     *
     * @param cycle cycle number for display
     * @throws SystemReliabilityException if two consecutive reliability failures
     *                                    occur
     */
    static void runCycle(int cycle) throws SystemReliabilityException {
        System.out.println("\n-- Cycle " + cycle + " --");
        log("-- Cycle " + cycle + " --");

        // poll all three sensors; null = failed
        Integer[] raw = new Integer[3];
        String[] ids = { "A", "B", "C" };
        for (int i = 0; i < 3; i++) {
            try {
                raw[i] = readSensor(ids[i]);
            } catch (SensorReadException e) {
                System.out.println(" Sensor Failure " + ids[i] + ": " + e.getMessage());
                log("SENSOR FAILURE: " + e.getMessage());
                raw[i] = null; // mark as failed
            }
        }

        // print raw readings
        System.out.printf("  Readings: A=%s  B=%s  C=%s%n",
                fmt(raw[0]), fmt(raw[1]), fmt(raw[2]));

        // collect valid (in-range) readings
        int[] vals = new int[3];
        String[] names = new String[3];
        int validCount = 0;
        for (int i = 0; i < 3; i++) {
            if (raw[i] != null && isValid(raw[i])) {
                vals[validCount] = raw[i];
                names[validCount] = ids[i];
                validCount++;
            } else if (raw[i] != null) {
                // out-of-range → corrupted
                System.out.println(" Sensor Error " + ids[i] + " value=" + raw[i]);
                log("CORRUPTED READING: Sensor " + ids[i] + " value=" + raw[i]);
            }
        }

        boolean failure = false;

        if (validCount < 2) {
            // not enough valid sensors to vote
            System.out.println("  [RELIABILITY] < 2 valid sensors – reliability failure");
            log("RELIABILITY FAILURE: < 2 valid sensors in cycle " + cycle);
            failure = true;

        } else {
            // majority vote: find any two equal values
            Integer majority = null;
            String outlier = "";
            for (int i = 0; i < validCount && majority == null; i++)
                for (int j = i + 1; j < validCount; j++)
                    if (vals[i] == vals[j]) {
                        majority = vals[i];
                        break;
                    }

            if (majority != null) {
                // find outlier (the sensor that disagrees)
                for (int i = 0; i < validCount; i++)
                    if (vals[i] != majority)
                        outlier = " [outlier: Sensor " + names[i] + "]";
                lastGood = majority; // update last known good altitude
                consecFails = 0; // reset failure counter
                System.out.println("  [VOTE] Altitude = " + majority + " m" + outlier);
                log("MAJORITY VOTE: altitude=" + majority + "m" + outlier);
                if (!outlier.isEmpty())
                    log("OUTLIER DETECTED: " + outlier.trim());
            } else {
                // all valid readings differ – no majority
                System.out.println("  [FALLBACK] No majority. Using last = " + lastGood + " m");
                log("FALLBACK DECISION: no majority, using altitude=" + lastGood + "m");
                failure = true;
            }
        }

        // track consecutive failures; escalate on second
        if (failure) {
            consecFails++;
            System.out.println("  [STATUS] Consecutive failures: " + consecFails);
            if (consecFails >= 2)
                throw new SystemReliabilityException("2 consecutive reliability failures");
        }
    }

    /**
     * Formats a nullable Integer for console display.
     *
     * @param v integer to format; may be {@code null}
     * @return string representation, or {@code "FAIL"} if {@code v} is null
     */
    static String fmt(Integer v) {
        return v == null ? "FAIL" : v.toString(); // null → "FAIL"
    }

    // ── Entry Point ───────────────────────────────────────────────────────────

    /**
     * Main method: initialises system, runs cycles inside try-catch.
     * Catches SystemReliabilityException to enter SAFE MODE.
     *
     * @param args unused
     */
    public static void main(String[] args) {
        String logFile = "log_" + (1000 + rng.nextInt(9000)) + ".txt";
        System.out.println("=== Drone Navigation System – ECTE331 ===");
        System.out.println("Log: " + logFile);

        try {
            log = new PrintWriter(new FileWriter(logFile), true); // open log
            log("=== System Started ===");

            for (int c = 1; c <= CYCLES; c++)
                runCycle(c); // runs until SAFE MODE or all cycles done

            System.out.println("\n[DONE] All " + CYCLES + " cycles completed.");

        } catch (SystemReliabilityException e) {
            // 2 consecutive failures → SAFE MODE
            System.out.println("\n*** SAFE MODE ACTIVATED ***");
            System.out.println("Reason: " + e.getMessage());
            log("SAFE MODE ACTIVATED: " + e.getMessage());

        } catch (IOException e) {
            System.err.println("[ERROR] " + e.getMessage());

        } finally {
            if (log != null) {
                log("=== System Shutdown ===");
                log.close();
            }
            System.out.println("Log written to: " + logFile);
        }
    } // main

} // class projectOne