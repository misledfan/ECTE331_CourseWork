class MathUtils {
    static long sumTo(int n) {
        long total = 0;
        for (int i = 0; i <= n; i++) {
            total += i;
        }
        return total;
    }
}

class SharedVars {
    static long A1, A2, A3, B1, B2, B3;
}

class Monitor {
    boolean a1Done = false;
    boolean b2Done = false;
    boolean a2Done = false;
    boolean b3Done = false;
}

class ThreadA extends Thread {
    private final Monitor mon;

    ThreadA(Monitor mon) {
        this.mon = mon;
    }

    private void funcA1() {
        SharedVars.A1 = MathUtils.sumTo(500);
        synchronized (mon) {
            mon.a1Done = true;
            mon.notifyAll();
        }
    }

    private void funcA2() throws InterruptedException {
        synchronized (mon) {
            while (!mon.b2Done)
                mon.wait();
        }
        SharedVars.A2 = SharedVars.B2 + MathUtils.sumTo(300);
        synchronized (mon) {
            mon.a2Done = true;
            mon.notifyAll();
        }
    }

    private void funcA3() throws InterruptedException {
        synchronized (mon) {
            while (!mon.b3Done)
                mon.wait();
        }
        SharedVars.A3 = SharedVars.B3 + MathUtils.sumTo(400);
    }

    public void run() {
        try {
            funcA1();
            funcA2();
            funcA3();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

class ThreadB extends Thread {
    private final Monitor mon;

    ThreadB(Monitor mon) {
        this.mon = mon;
    }

    private void funcB1() {
        SharedVars.B1 = MathUtils.sumTo(250);
    }

    private void funcB2() throws InterruptedException {
        synchronized (mon) {
            while (!mon.a1Done)
                mon.wait();
        }
        SharedVars.B2 = SharedVars.A1 + MathUtils.sumTo(200);
        synchronized (mon) {
            mon.b2Done = true;
            mon.notifyAll();
        }
    }

    private void funcB3() throws InterruptedException {
        synchronized (mon) {
            while (!mon.a2Done)
                mon.wait();
        }
        SharedVars.B3 = SharedVars.A2 + MathUtils.sumTo(400);
        synchronized (mon) {
            mon.b3Done = true;
            mon.notifyAll();
        }
    }

    public void run() {
        try {
            funcB1();
            funcB2();
            funcB3();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

public class projectTwo {
    public static void main(String[] args) throws InterruptedException {
        Monitor mon = new Monitor();
        ThreadA tA = new ThreadA(mon);
        ThreadB tB = new ThreadB(mon);

        tA.start();
        tB.start();
        tA.join();
        tB.join();

        System.out.println("A1: " + SharedVars.A1);
        System.out.println("B1: " + SharedVars.B1);
        System.out.println("B2: " + SharedVars.B2);
        System.out.println("A2: " + SharedVars.A2);
        System.out.println("B3: " + SharedVars.B3);
        System.out.println("A3: " + SharedVars.A3);
    }
}