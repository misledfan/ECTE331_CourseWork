public class SafetyMonitor extends Thread {
    private final MotorController motorController;

    public SafetyMonitor(MotorController motorController) {
        this.motorController = motorController;
        this.setName("Safety Monitor");
        // Task 1: Assign appropriate priorities
        this.setPriority(Thread.MAX_PRIORITY); // High priority
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            long timestamp = System.currentTimeMillis();
            System.out.println("[" + timestamp + "] " + getName() + " is executing and attempting to access MotorController...");
            
            motorController.accessMotor(getName());
            
            try {
                // Sleep to simulate interval between checking for emergency conditions
                Thread.sleep(1000); 
            } catch (InterruptedException e) {
                // Task was interrupted, likely to stop the simulation
                System.out.println("[" + System.currentTimeMillis() + "] " + getName() + " shutting down.");
                break;
            }
        }
    }
}
