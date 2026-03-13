package ThreadPools;
import java.util.concurrent.*;

public class Executioner {  
    public static void main(String[] args) throws InterruptedException, ExecutionException {
        // Create an office with 2 workers
        ExecutorService executor = Executors.newFixedThreadPool(2);
        System.out.println("Main: Office is open with 2 workers.");

        // Task 1: Takes 10 seconds
        Future<?> f = executor.submit(() -> {
            String name = Thread.currentThread().getName();
            System.out.println(name + " is starting Task A (takes 10s)...");
            int count = 10;
            while(count-->0)
            {
                System.out.println("Task A running...");
                Thread.sleep(1000);
            }
            System.out.println(name + " finished Task A.");
            return null;
        }); 

        // Task 2: Takes 10 second
        Future<Integer> s = executor.submit(() -> {
            String name = Thread.currentThread().getName();
            System.out.println(name + " is starting Task B (takes 10s)...");
            
            int count = 10;
            while(count-->0)
            {
                System.out.println("Task B running...");
                Thread.sleep(1000);
            }
            System.out.println(name + " finished Task B.");
            return 5;
        });

        // Task 3: Takes 60 second
        Future<Integer> e = executor.submit(() -> {
            String name = Thread.currentThread().getName();
            System.out.println(name + " is starting Task C (takes 10s)...");
            
            int count = 60;
            while(count-->0)
            {
                System.out.println("Task C running...");
                Thread.sleep(1000);
            }
            System.out.println(name + " finished Task C.");
            return 111;
        });

        System.out.println("Main: I've handed out all tasks. Now I'm going to wait.");
        
        // Close the office after tasks are done
        executor.shutdown();
        
        Integer taskCResult = e.get();

        
        System.out.println("Task C Result "+taskCResult);
    }
}