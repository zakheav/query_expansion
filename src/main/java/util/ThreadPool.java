package util;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class ThreadPool {
    private int SIZE = 10;
    private final List<Thread> pool;
    private final Queue<Runnable> tasks;
    private static ThreadPool instance = new ThreadPool();

    private ThreadPool() {
        pool = new ArrayList<>();
        tasks = new LinkedList<>();
        for (int i = 0; i < SIZE; ++i) {
            addLabour(new Worker());
        }
    }

    public static ThreadPool getInstance() {
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
                }
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

    public void addTasks(Runnable task) {
        synchronized (tasks) {
            tasks.offer(task);
            tasks.notify();
        }
    }

    public void shutDown() {
        for (Thread thread : pool) {
            thread.stop();
        }
    }
}
