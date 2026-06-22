public class MotorController {
    private final boolean useSynchronization;

    public MotorController(boolean useSynchronization) {
        this.useSynchronization = useSynchronization;
    }

    public MotorController() {
        this(true); // Default to synchronized for safety
    }

    // Single access point that uses the flag to determine synchronization
    public void accessMotor(String threadName) {
        if (useSynchronization) {
            synchronized (this) {
                performMotorAction(threadName);
            }
        } else {
            performMotorAction(threadName);
        }
    }

    private void performMotorAction(String threadName) {
        long timestamp = System.currentTimeMillis();
        System.out.println("[" + timestamp + "] " + threadName + " has acquired the MotorController.");
        
        try {
            // Simulate work with the motor
            Thread.sleep(500); 
        } catch (InterruptedException e) {
            System.out.println("[" + System.currentTimeMillis() + "] " + threadName + " was interrupted while accessing motor.");
        }
        
        timestamp = System.currentTimeMillis();
        System.out.println("[" + timestamp + "] " + threadName + " has released the MotorController.");
    }
}
