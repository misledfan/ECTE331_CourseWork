public class Task1 {
    public static void main(String[] args) {
        System.out.println("--- Task 1: Basic Multi-threaded Implementation ---");
        
        // Pass 'false' to disable synchronization for Task 1, demonstrating potential race conditions
        MotorController sharedMotor = new MotorController(false);
        
        Logger logger = new Logger(sharedMotor);
        MotionPlanner planner = new MotionPlanner(sharedMotor);
        SafetyMonitor monitor = new SafetyMonitor(sharedMotor);

        logger.start();
        planner.start();
        monitor.start();

        try {
            Thread.sleep(5000); // Run simulation for 5 seconds
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("\nStopping Task 1 Simulation...");
        logger.interrupt();
        planner.interrupt();
        monitor.interrupt();
    }
}
