package kakha.kudava.primecountermultithread.interactions;

import kakha.kudava.primecountermultithread.MainPage;
import kakha.kudava.primecountermultithread.controller.MainPageController;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class ThreadStopper {

    private boolean stopping;
    private Object PAUSE_LOCK;
    private boolean paused;
    private Thread producer;
    private List<Thread> consumers;
    private BlockingQueue<String> queue;
    private String STOP;
    private List<Integer> primeCounts;
    private AtomicInteger counter;
    private AtomicInteger threadId;
    private AtomicInteger maxConsumers;

    public ThreadStopper(boolean stopping, Object pauseLock, boolean paused, Thread producer,
                         List<Thread> consumers, BlockingQueue<String> queue, String STOP,
                         List<Integer> primeCounts, AtomicInteger counter, AtomicInteger threadId,
                         AtomicInteger maxConsumers) {
        this.paused = paused;
        this.producer = producer;
        this.consumers = consumers;
        this.queue = queue;
        this.STOP = STOP;
        this.primeCounts = primeCounts;
        this.counter = counter;
        this.threadId = threadId;
        this.maxConsumers = maxConsumers;
        this.stopping = stopping;
        this.PAUSE_LOCK = pauseLock;

    }

    public void setProducer(Thread producer) {
        this.producer = producer;
    }
    public void stopThreadsExec() throws InterruptedException {
        // tell everyone we're stopping
        stopping = true;

        // if paused, wake everyone so they can see 'stopping'
        synchronized (PAUSE_LOCK) {
            paused = false;
            PAUSE_LOCK.notifyAll();
        }

        // stop the producer and wait for it to finish
        if (producer != null) {
            producer.interrupt();
            try {
                producer.join(1000); // wait up to 1s
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            producer = null;
        }

        // snapshot current consumers (so list won't change under us)
        List<Thread> snapshot = new ArrayList<>(consumers);

        // count how many are still alive
        int alive = 0;
        for (Thread t : snapshot) {
            if (t.isAlive()) {
                alive++;
            }
        }

        // clear any pending work and send one STOP per alive consumer
        queue.clear();
        for (int i = 0; i < alive; i++) {
            try {
                queue.put(STOP);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        //interrupt and join consumers so they exit promptly
        for (Thread t : snapshot) {
            if (t.isAlive()) {
                t.interrupt();
                try {
                    t.join(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }

        // final cleanup of collections and counters
        queue.clear();
        consumers.clear();
        synchronized (primeCounts) {
            primeCounts.clear();
        }
        counter.set(0);
        threadId.set(1);
        maxConsumers.set(0);

        MainPageController c = MainPage.controller;
        if (c != null) {
            c.setCurrentThreadLabel(0);
            // you can also clear UI rows here if you want:
            // Platform.runLater(() -> c.clearAllThreadUI());
        }

        System.out.println("All threads stopped, state reset.");
    }

    public void pauseThreads() {
        synchronized (PAUSE_LOCK) {
            paused = true;
        }
    }

    public void resumeThreads() {
        synchronized (PAUSE_LOCK) {
            paused = false;
            PAUSE_LOCK.notifyAll();
        }
    }

    public boolean isPaused() {
        return paused;
    }

    public void waitIfPaused() throws InterruptedException {
        synchronized (PAUSE_LOCK) {
            while (paused && !stopping) {
                PAUSE_LOCK.wait();
            }
        }
    }
}
