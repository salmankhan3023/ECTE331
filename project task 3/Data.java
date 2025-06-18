package proj_task3;

class Data {
    int A1, A2, A3, B1, B2, B3;
    boolean A1_done = false;
    boolean B2_done = false;
    boolean A2_done = false;
    boolean B3_done = false;
}

class Utility {
    public static int calculate(int n) {
        return (n * (n + 1)) / 2;
    }
}

class ThreadA extends Thread {
    private Data data;
    
    public ThreadA(Data data) {
        this.data = data;
    }
    
    public void run() {
        synchronized (data) {
            data.A1 = Utility.calculate(500);
            System.out.println("A1 completed: " + data.A1);
            data.A1_done = true;
            data.notifyAll();
        }
        
        synchronized (data) {
            try {
                while (!data.B2_done) {
                    data.wait();
                }
                data.A2 = data.B2 + Utility.calculate(300);
                System.out.println("A2 completed: " + data.A2);
                data.A2_done = true;
                data.notifyAll();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        
        synchronized (data) {
            try {
                while (!data.B3_done) {
                    data.wait();
                }
                data.A3 = data.B3 + Utility.calculate(400);
                System.out.println("A3 completed: " + data.A3);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}

class ThreadB extends Thread {
    private Data data;
    
    public ThreadB(Data data) {
        this.data = data;
    }
    
    public void run() {

        synchronized (data) {
            data.B1 = Utility.calculate(250);
            System.out.println("B1 completed: " + data.B1);
        }
        
        synchronized (data) {
            try {
                while (!data.A1_done) {
                    data.wait();
                }
                data.B2 = data.A1 + Utility.calculate(200);
                System.out.println("B2 completed: " + data.B2);
                data.B2_done = true;
                data.notifyAll();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        synchronized (data) {
            try {
                while (!data.A2_done) {
                    data.wait();
                }
                data.B3 = data.A2 + Utility.calculate(400);
                System.out.println("B3 completed: " + data.B3);
                data.B3_done = true;
                data.notifyAll();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}

class ThreadC extends Thread {
    private Data data;
    
    public ThreadC(Data data) {
        this.data = data;
    }
    
    public void run() {
        synchronized (data) {
            try {
                while (!data.B3_done) {
                    data.wait();
                }
                int result = data.A2 + data.B3;
                System.out.println("Thread C result (A2 + B3): " + result);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}


