package proj_task3;

public class Question3 {
    public static void main(String[] args) throws InterruptedException {
        final int ITERATIONS = 1000;
        
        System.out.println("Running " + ITERATIONS + " iterations to verify correctness...");
        
        for (int i = 0; i < ITERATIONS; i++) {
            Data data = new Data();
            
            ThreadA tA = new ThreadA(data);
            ThreadB tB = new ThreadB(data);
            ThreadC tC = new ThreadC(data);
            
            tA.start();
            tB.start();
            tC.start();
            
            tA.join();
            tB.join();
            tC.join();
            }
    }
}
