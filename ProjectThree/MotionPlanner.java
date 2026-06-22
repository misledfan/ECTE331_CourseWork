public class MotionPlanner extends Thread {
    private final MotorController motorController;

    public MotionPlanner(MotorController motorController) {
        this.motorController = motorController;
        this.setName("Motion Planner");
        // Task 1: Assign appropriate priorities
        this.setPriority(Thread.NORM_PRIORITY); // Medium priority
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            long timestamp = System.currentTimeMillis();
            System.out.println("[" + timestamp + "] " + getName() + " is executing and attempting to access MotorController...");
            
            motorController.accessMotor(getName());
            
            try {
                // Sleep to simulate time between movement commands
                Thread.sleep(1500); 
            } catch (InterruptedException e) {
                // Task was interrupted, likely to stop the simulation
                System.out.println("[" + System.currentTimeMillis() + "] " + getName() + " shutting down.");
                break;
            }
        }
    }
}
