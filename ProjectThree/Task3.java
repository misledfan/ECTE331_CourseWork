public class Task3 {
    static class MotorController {
        public synchronized void accessMotor(String threadName, long duration) {
            System.out.println("[" + System.currentTimeMillis() + "] " + threadName + " acquired the MotorController.");
            try { Thread.sleep(duration); } catch (InterruptedException e) {}
            System.out.println("[" + System.currentTimeMillis() + "] " + threadName + " released the MotorController.");
        }
    }

    public static void main(String[] args) {
        System.out.println("--- Task 3: Priority Inversion Demonstration ---");
        MotorController motor = new MotorController();

        Thread lowPriority = new Thread(() -> {
            motor.accessMotor("Logger (Low)", 3000); // Hold for 3 seconds
        });
        lowPriority.setName("Logger");
        lowPriority.setPriority(Thread.MIN_PRIORITY);

        Thread highPriority = new Thread(() -> {
            long waitStart = System.currentTimeMillis();
            System.out.println("[" + waitStart + "] Safety Monitor (High) requesting MotorController...");
            motor.accessMotor("Safety Monitor (High)", 500);
            long waitTime = System.currentTimeMillis() - waitStart;
            System.out.println(">>> Safety Monitor (High) waiting time: " + waitTime + " ms <<<");
        });
        highPriority.setName("Safety Monitor");
        highPriority.setPriority(Thread.MAX_PRIORITY);

        Thread mediumPriority = new Thread(() -> {
            System.out.println("[" + System.currentTimeMillis() + "] Motion Planner (Medium) started executing (Simulating Busy Work)...");
            long endTime = System.currentTimeMillis() + 5000;
            // Busy wait to preempt the Low priority thread and delay the release of the resource
            while (System.currentTimeMillis() < endTime) { }
            System.out.println("[" + System.currentTimeMillis() + "] Motion Planner (Medium) finished executing.");
        });
        mediumPriority.setName("Motion Planner");
        mediumPriority.setPriority(Thread.NORM_PRIORITY);

        // Orchestrate the scenario
        lowPriority.start();
        try { Thread.sleep(200); } catch (InterruptedException e) {} // Give low priority time to acquire the lock
        
        highPriority.start();
        try { Thread.sleep(200); } catch (InterruptedException e) {} // Give high priority time to request lock and block
        
        mediumPriority.start(); // Medium priority starts, preempting low priority and causing priority inversion

        try {
            lowPriority.join();
            highPriority.join();
            mediumPriority.join();
        } catch (InterruptedException e) {}
        
        System.out.println("Task 3 Completed.");
    }
}
