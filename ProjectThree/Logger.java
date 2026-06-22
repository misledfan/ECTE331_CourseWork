public class Logger extends Thread {
    private final MotorController motorController;

    public Logger(MotorController motorController) {
        this.motorController = motorController;
        this.setName("Logger");
        // Task 1: Assign appropriate priorities
        this.setPriority(Thread.MIN_PRIORITY); // Low priority
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            long timestamp = System.currentTimeMillis();
            System.out.println("[" + timestamp + "] " + getName() + " is executing and attempting to access MotorController...");
            
            motorController.accessMotor(getName());
            
            try {
                // Sleep to simulate interval between logging operations
                Thread.sleep(2000); 
            } catch (InterruptedException e) {
                // Task was interrupted, likely to stop the simulation
                System.out.println("[" + System.currentTimeMillis() + "] " + getName() + " shutting down.");
                break;
            }
        }
    }
}
