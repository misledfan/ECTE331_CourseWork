import java.io.*;
import java.time.LocalDateTime;
import java.util.Random;

class SensorReadException extends IOException {
    public SensorReadException(String msg) {
        super(msg);
    }
}

class SystemReliabilityException extends Exception {
    public SystemReliabilityException(String msg) {
        super(msg);
    }
}

public class projectOne {
    static final int MIN = 0;
    static final int MAX = 200;
    static final int BASELINE = 100;
    static final int CYCLES = 10;
    static Random rng = new Random();
    static PrintWriter log;
    static int lastGood = BASELINE;
    static int consecFails = 0;

    static int readSensor(String id) throws SensorReadException {
        int chance = rng.nextInt(100);
        if (chance < 15)
            throw new SensorReadException("Sensor " + id + " failure");
        else if (chance < 30)
            return MAX + 1 + rng.nextInt(100);
        else
            return BASELINE + rng.nextInt(21) - 10;
    }

    static boolean isValid(int v) {
        return v >= MIN && v <= MAX;
    }

    static void log(String msg) {
        log.println("[" + LocalDateTime.now().toString().substring(0, 19) + "] " + msg);
    }

    static void runCycle(int cycle) throws SystemReliabilityException {
        System.out.println("\n-- Cycle " + cycle + " --");
        log("-- Cycle " + cycle + " --");

        Integer[] raw = new Integer[3];
        String[] ids = { "A", "B", "C" };
        for (int i = 0; i < 3; i++) {
            try {
                raw[i] = readSensor(ids[i]);
            } catch (SensorReadException e) {
                System.out.println(" Sensor " + ids[i] + " failed");
                log("SENSOR FAILURE: " + ids[i]);
                raw[i] = null;
            }
        }

        System.out.printf(" Readings: A=%s B=%s C=%s%n", fmt(raw[0]), fmt(raw[1]), fmt(raw[2]));

        int[] vals = new int[3];
        String[] names = new String[3];
        int validCount = 0;
        for (int i = 0; i < 3; i++) {
            if (raw[i] != null && isValid(raw[i])) {
                vals[validCount] = raw[i];
                names[validCount] = ids[i];
                validCount++;
            }
        }

        boolean failure = false;
        if (validCount < 2) {
            failure = true;
        } else {
            Integer majority = null;
            for (int i = 0; i < validCount && majority == null; i++)
                for (int j = i + 1; j < validCount; j++)
                    if (vals[i] == vals[j])
                        majority = vals[i];

            if (majority != null) {
                lastGood = majority;
                consecFails = 0;
                System.out.println(" [VOTE] Altitude = " + majority + "m");
            } else {
                failure = true;
            }
        }

        if (failure) {
            consecFails++;
            if (consecFails >= 2)
                throw new SystemReliabilityException("2 consecutive failures");
        }
    }

    static String fmt(Integer v) {
        return v == null ? "FAIL" : v.toString();
    }

    public static void main(String[] args) {
        String logFile = "log_" + (1000 + rng.nextInt(9000)) + ".txt";
        try {
            log = new PrintWriter(new FileWriter(logFile), true);
            for (int c = 1; c <= CYCLES; c++)
                runCycle(c);
        } catch (SystemReliabilityException e) {
            System.out.println("\n*** SAFE MODE: " + e.getMessage() + " ***");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (log != null)
                log.close();
        }
    }
}