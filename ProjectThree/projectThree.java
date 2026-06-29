public class projectThree {
    public static void main(String[] args) {
        System.out.println("Starting Robotic Arm Controller Simulation (Task 1 & 2)...");
        System.out.println("=========================================================");
        
        // Task 1: Implement the shared MotorController resource
        MotorController sharedMotor = new MotorController();
        
        // Task 1: Implement the three Real-time threads
        SafetyMonitor safetyMonitor = new SafetyMonitor(sharedMotor);
        MotionPlanner motionPlanner = new MotionPlanner(sharedMotor);
        Logger logger = new Logger(sharedMotor);
        
        // Start the threads
        logger.start();
        motionPlanner.start();
        safetyMonitor.start();
        
        // Let the simulation run for a set duration (e.g., 10 seconds)
        try {
            Thread.sleep(10000); 
        } catch (InterruptedException e) {
            System.err.println("Main thread interrupted.");
        }
        
        // Stop the simulation
        System.out.println("\nStopping Simulation...");
        safetyMonitor.interrupt();
        motionPlanner.interrupt();
        logger.interrupt();
        
        // Wait for threads to finish
        try {
            safetyMonitor.join();
            motionPlanner.join();
            logger.join();
        } catch (InterruptedException e) {
            System.err.println("Main thread interrupted while waiting for child threads.");
        }
        
        System.out.println("Simulation complete.");
    }
}
