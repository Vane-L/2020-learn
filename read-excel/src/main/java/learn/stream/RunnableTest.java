package learn.stream;

/**
 * @Author: wenhongliang
 */
public class RunnableTest {
    public static void main(String[] args) {
        Runner1 runner1 = new Runner1();
        Runner2 runner2 = new Runner2();

        Thread thread1 = new Thread(runner1);
        Thread thread2 = new Thread(runner2);

        //thread1.start();
        //thread2.start();

        thread1.run();
        thread2.run();

        Worker1 worker1 = new Worker1();
        worker1.setTask(() -> {
            for (int i = 0; i < 10; i++) {
                System.out.println("Runnable task run " + Thread.currentThread().getName());
            }
        });
        Thread thread = new Thread(worker1);
        thread.setName("Worker");
        thread.start();
    }

    static class Worker1 implements Runnable {
        Runnable task;

        public void setTask(Runnable task) {
            this.task = task;
        }

        @Override
        public void run() {
            runSelf(this);
        }

        private void runSelf(Worker1 worker1) {
            worker1.task.run();
        }
    }

    static class Runner1 implements Runnable {

        @Override
        public void run() {
            for (int i = 0; i < 100; i++) {
                System.out.println("Runnable 1 run " + i);
            }
        }
    }

    static class Runner2 implements Runnable {

        @Override
        public void run() {
            for (int i = 0; i < 100; i++) {
                System.out.println("Runnable 2 run" + i);
            }
        }
    }
}
