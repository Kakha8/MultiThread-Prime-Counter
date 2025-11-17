package kakha.kudava.primecountermultithread;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static kakha.kudava.primecountermultithread.FileNums.*;

public class ConsumerThread {


    public static BlockingQueue<String> queue = new LinkedBlockingDeque<>(1000);
    public static List<Thread> consumers = new CopyOnWriteArrayList<>();
    private static List<Integer> primeCounts = new ArrayList<Integer>();
    private static List<Boolean> threadCount = new ArrayList<Boolean>();
    private static AtomicInteger threadId = new AtomicInteger(1);
    public static final String STOP = "STOP";

    public static AtomicInteger maxConsumers = new AtomicInteger(0);
    private static final Object PAUSE_LOCK = new Object();
    private static volatile boolean paused = false;
    private static boolean stopping = false;

    private static Thread producer;

    public static void pauseThreads() {
        synchronized (PAUSE_LOCK) {
            paused = true;
        }
    }

    public static void resumeThreads() {
        synchronized (PAUSE_LOCK) {
            paused = false;
            PAUSE_LOCK.notifyAll();
        }
    }

    public static boolean isPaused() {
        return paused;
    }

    private static void waitIfPaused() throws InterruptedException {
        synchronized (PAUSE_LOCK) {
            while (paused && !stopping) {
                PAUSE_LOCK.wait();
            }
        }
    }

    public static void startConsumerThread() {

        int id = threadId.getAndIncrement();

        Thread consumer = new Thread(() -> {
            Thread.currentThread().setName("Consumer-" + id);
            List<Integer> primeNums = new ArrayList<>();

            try {
                // respect pause before doing any work
                if (isPaused()) {
                    System.out.println(Thread.currentThread().getName() + " waiting (paused)...");
                }
                waitIfPaused();
                if (stopping) return;   // just in case someone called stop

                // ----- ONE file per thread -----
                // block until we get one item
                String item = queue.take();   // no loop, no poll()

                // if you still sometimes put STOP into the queue, just ignore it
                if (STOP.equals(item)) {
                    System.out.println(Thread.currentThread().getName() + " got STOP, exiting.");
                    return;
                }

                // process THIS file only
                List<Integer> nums = getNums(item);
                for (Integer num : nums) {
                    if (isPrime(num)) primeNums.add(num);
                }

                System.out.println(Thread.currentThread().getName() + " processed ONE file");
                System.out.println("Consuming " + primeNums);
                System.out.println(primeNums.size() + " of " + nums.size());

                synchronized (primeCounts) {
                    primeCounts.add(primeNums.size());
                }

                int maxCount = getMaxPrimeCount(primeCounts);
                int maxPrime = getMaxPrimeCount(primeNums);

                synchronized (threadCount) {
                    threadCount.add(true);   // now: one entry per thread
                }

                System.out.println("max count: " + maxCount);
                System.out.println("max prime: " + maxPrime);

                MainPageController c = MainPage.controller;
                if (c != null) {
                    c.showMax(id, primeNums.size(), nums.size());
                    c.counter(maxPrime, maxCount, threadCount.size());
                    c.setCurrentThreadLabel(threadCount.size());
                } else {
                    System.out.println("Controller not ready yet");
                }

                // thread ends here — no while loop, no extra files
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        consumer.start();
        consumers.add(consumer);
    }

    public static void addMoreConsumers(int howMany) {
        // don't spawn anything if we are shutting down
        if (stopping) {
            System.out.println("Not adding consumers: stopping == true");
            return;
        }

        // update the maxConsumers counter (optional but consistent with your design)
        maxConsumers.addAndGet(howMany);

        for (int i = 0; i < howMany; i++) {
            startConsumerThread();
        }

        System.out.println("Added " + howMany + " more consumers. Total: " + consumers.size());
    }


    public static void producerConsumer(int threadCountNum) throws InterruptedException {

        //List<Thread> consumers = new CopyOnWriteArrayList<>();

        stopping = false;
        maxConsumers.set(threadCountNum);
        //System.out.println(maxConsumers);
        producer = new Thread(() -> {
            try {
                String content = new String();

                for (int i = 1; i < 1001 + 1; i++) {
                    System.out.println("Producing: " + i);

                    content = Files.readString(Path.of("data-files/file" + String.valueOf(i) + ".txt"));

                    // respecting pause between attempts
                    while (true) {
                        waitIfPaused();
                        if (queue.offer(content, 200, TimeUnit.MILLISECONDS)) break;
                    }
                    Thread.sleep(100);
                }

                for (int i = 0; i < consumers.size(); i++) {
                    queue.put(STOP);
                }

            } catch (InterruptedException | IOException e){
                Thread.currentThread().interrupt();
            }
        });

        System.out.println("threadcountnum: " + threadCountNum);
        for (int i = 1; i < threadCountNum + 1; i++){
            startConsumerThread();
        }


        producer.start();
    }
    public static void stopThreads() {
        // 1) flip the flag
        stopping = true;

        // 2) if paused, wake everyone so they can see 'stopping' or consume STOP
        synchronized (PAUSE_LOCK) {
            paused = false;
            PAUSE_LOCK.notifyAll();
        }

        // 3) stop the producer quickly
        if (producer != null && producer.isAlive()) {
            producer.interrupt();
        }

        // 4) send one STOP per currently alive consumer
        int n = consumers.size();
        for (int i = 0; i < n; i++) {
            try {
                // use put() to guarantee delivery; bounded queue handles backpressure
                queue.put(STOP);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        // 5) interrupt consumers in case any are sleeping/blocking
        for (Thread t : consumers) {
            if (t.isAlive()) t.interrupt();
        }

        // 6) join consumers (give them a moment to exit cleanly)
        for (Thread t : consumers) {
            try {
                t.join(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        // 7) now it's safe to clear state
        queue.clear();
        consumers.clear();
        // if you keep shared lists/counters, reset them here too
        // primeCounts.clear(); threadCount.clear(); etc.

        // 8) producer reference is no longer useful
        producer = null;
    }


    public static void adjustThreads(int selectedThreadAdjust) {
        MainPageController c = MainPage.controller;
        if (c == null) return;

        new Thread(() -> {

                int currentThread = c.getCurrentThread();
                int currentThreadMax = maxConsumers.get();
                if(selectedThreadAdjust > currentThreadMax) {
                    int diff = selectedThreadAdjust - currentThreadMax;
                    try {
                        System.out.println(diff);
                        ThreadAdjust.addThreads(diff, maxConsumers);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
                if (selectedThreadAdjust < currentThreadMax) {
                    int diff = currentThreadMax - selectedThreadAdjust;
                    ThreadAdjust.removeConsumers(diff);
                }



        }, "AdjustThreadWatcher").start();
    }

}
