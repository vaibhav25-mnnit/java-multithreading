
class ThreadExtendedClass extends Thread {
    private String threadName = "ExtendedThread";

    public ThreadExtendedClass() {
    }

    public ThreadExtendedClass(String name) {
        this.threadName = name;
    }

    @Override
    public void run() {

        while (true)
        {
            System.out.println(" Thread Name: " + threadName + " is running.");
            
            try {
                Thread.sleep(1000);
            } catch (Exception e) {
                System.out.println("Extended Thread interrupted: " + e.getMessage());
            }
        }
    }
}

class ThreadRunnableClass implements Runnable {
    private String threadName = "RunnableThread";

    public ThreadRunnableClass() {
    }

    public ThreadRunnableClass(String name) {
        this.threadName = name;
    }

    @Override
    public void run() {
        while (true)
        {
            System.out.println(" Thread Name: " + threadName + " is running.");
            
            try {
                Thread.sleep(1000);
            } catch (Exception e) {
                System.out.println("Runnable Thread interrupted: " + e.getMessage());
            }
        }
    }
}

public class Main {
    public static void main(String[] args) {
        System.out.println("Threading Main Class");
        ThreadExtendedClass thread1 = new ThreadExtendedClass("Class Extended Thread");
        /*
            Default priority is 5,
            Range is from 5 to 10.
            But chaning the priority does not guarantee that it is scheduled at earlist.
        */
        System.out.println(thread1.getPriority());
        thread1.start();

        Thread thread2 = new Thread(new ThreadRunnableClass("Runnable Thread"));
        thread2.start();

        /*
            --> Thread using lambda 

            ->This works because the Runnable interface is having only one method 'run' and is a Functional Interface

            -->Thread Class constructor accepts Runnable object which can be made using the anonymous class which can further be done using lambda expression
        */
        Thread thread3 = new Thread(() -> {
                    while (true)
                    {
                        System.out.println(" Thread Name: Anonymous Class Thread running.");
                        
                        try {
                            Thread.sleep(1000);
                        } catch (Exception e) {
                            System.out.println("Runnable Thread interrupted: " + e.getMessage());
                        }
                    }
                }
        );
        thread3.start();

        thread1.join();

    }
}
    