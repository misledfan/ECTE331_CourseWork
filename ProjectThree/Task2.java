public class Task2 {
    public static void main(String[] args) {
        System.out.println("--- Task 2: Synchronization ---");
        
        // Pass 'true' to enable synchronization for Task 2, ensuring mutual exclusion
        MotorController sharedMotor = new MotorController(true);
        
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

        System.out.println("\nStopping Task 2 Simulation...");
        logger.interrupt();
        planner.interrupt();
        monitor.interrupt();
    }
}
