public class Task5 {
    // Custom Lock to simulate Priority Ceiling Protocol
    static class PriorityCeilingMutex {
        private Thread owner = null;
        private int originalPriority = -1;
        private final int ceilingPriority = Thread.MAX_PRIORITY;

        public synchronized void lock() {
            Thread current = Thread.currentThread();
            while (owner != null) {
                try { wait(); } catch (InterruptedException e) {}
            }
            owner = current;
            originalPriority = current.getPriority();
            if (current.getPriority() < ceilingPriority) {
                System.out.println("[" + System.currentTimeMillis() + "] Priority Ceiling: Raising " + owner.getName() + "'s priority to ceiling (" + ceilingPriority + ")");
                owner.setPriority(ceilingPriority);
            }
        }

        public synchronized void unlock() {
            if (owner == Thread.currentThread()) {
                if (owner.getPriority() != originalPriority) {
                    System.out.println("[" + System.currentTimeMillis() + "] Priority Ceiling: Restoring " + owner.getName() + "'s priority to " + originalPriority);
                    owner.setPriority(originalPriority);
                }
                owner = null;
                notifyAll();
            }
        }
    }

    static class MotorController {
        private final PriorityCeilingMutex mutex = new PriorityCeilingMutex();
        
        public void accessMotor(String threadName, long duration) {
            mutex.lock();
            System.out.println("[" + System.currentTimeMillis() + "] " + threadName + " acquired the MotorController.");
            try { Thread.sleep(duration); } catch (InterruptedException e) {}
            System.out.println("[" + System.currentTimeMillis() + "] " + threadName + " released the MotorController.");
            mutex.unlock();
        }
    }

    public static void main(String[] args) {
        System.out.println("--- Task 5: Priority Ceiling Protocol ---");
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
            while (System.currentTimeMillis() < endTime) { }
            System.out.println("[" + System.currentTimeMillis() + "] Motion Planner (Medium) finished executing.");
        });
        mediumPriority.setName("Motion Planner");
        mediumPriority.setPriority(Thread.NORM_PRIORITY);

        // Orchestrate the scenario
        lowPriority.start();
        try { Thread.sleep(200); } catch (InterruptedException e) {}
        
        highPriority.start();
        try { Thread.sleep(200); } catch (InterruptedException e) {}
        
        mediumPriority.start();

        try {
            lowPriority.join();
            highPriority.join();
            mediumPriority.join();
        } catch (InterruptedException e) {}
        
        System.out.println("Task 5 Completed.");
    }
}
