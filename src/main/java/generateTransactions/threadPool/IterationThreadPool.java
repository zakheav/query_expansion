package generateTransactions.threadPool;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class IterationThreadPool {
    private int SIZE = 4;
    private final List<Thread> pool;
    private final Queue<Runnable> tasks;
    private static IterationThreadPool instance = new IterationThreadPool();
    private SequenceNum finishTaskNum;
    private Logger logger = Logger.getLogger(IterationThreadPool.class);

    private IterationThreadPool() {
        pool = new ArrayList<>();
        tasks = new LinkedList<>();
        for (int i = 0; i < SIZE; ++i) {
            addLabour(new Worker());
        }
    }

    public static IterationThreadPool getInstance() {
        return instance;
    }

    class Worker extends Thread {
        public void run() {
            while (true) {
                Runnable task;
                synchronized (tasks) {
                    while (tasks.isEmpty()) {
                        try {
                            tasks.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                            return;
                        }
                    }
                    task = tasks.poll();
                }
                try {
                    task.run();
                } catch (Exception e) {
                    e.printStackTrace();
                    logger.error(e.getMessage());
                }
                finishTaskNum.increase();
            }
        }
    }

    private void addLabour(Thread t) {
        synchronized (this.pool) {
            if (pool.size() < SIZE) {
                pool.add(t);
                pool.get(pool.size() - 1).start();
            }
        }
    }

    private void addTask(Runnable task) {
        synchronized (tasks) {
            tasks.offer(task);
            tasks.notify();
        }
    }

    public void addTasks(List<Runnable> tasks) {
        finishTaskNum = new SequenceNum();
        for (Runnable task : tasks) {
            addTask(task);
        }
        while (finishTaskNum.get() < tasks.size()) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void shutDown() {
        for (Thread thread : pool) {
            thread.stop();
        }
    }
}
