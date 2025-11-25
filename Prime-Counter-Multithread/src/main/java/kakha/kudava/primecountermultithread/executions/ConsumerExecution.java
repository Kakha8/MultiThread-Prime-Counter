package kakha.kudava.primecountermultithread.executions;

import kakha.kudava.primecountermultithread.services.Counters;
import kakha.kudava.primecountermultithread.MainPage;
import kakha.kudava.primecountermultithread.interactions.ThreadStopper;
import kakha.kudava.primecountermultithread.controller.MainPageController;
import kakha.kudava.primecountermultithread.services.PrimesResultWriter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import static kakha.kudava.primecountermultithread.services.FileNums.*;
import static kakha.kudava.primecountermultithread.services.FileNums.getMaxPrimeCount;

public class ConsumerExecution implements Runnable {

    private ThreadStopper threadStopper;
    private int id;
    private String STOP;
    private boolean stopping;
    private BlockingQueue<String> queue;
    private AtomicInteger fileCounter;
    private List<Integer> primeCounts;
    private AtomicInteger counter;

    private PrimesResultWriter writer = new PrimesResultWriter();

    private AtomicInteger maxConsumers;
    private AtomicInteger globalMaxPrime;
    private AtomicInteger globalMinPrime;
    private AtomicInteger activeConsumers;
    public ConsumerExecution(ThreadStopper threadStopper, int id, String STOP, boolean stopping,
                             BlockingQueue<String> queue, AtomicInteger fileCounter,
                             List<Integer> primeCounts, AtomicInteger counter, AtomicInteger maxConsumers,
                             AtomicInteger globalMaxPrime, AtomicInteger globalMinPrime, AtomicInteger activeConsumers) {
        this.threadStopper = threadStopper;
        this.id = id;
        this.STOP = STOP;
        this.stopping = stopping;
        this.queue = queue;
        this.fileCounter = fileCounter;
        this.primeCounts = primeCounts;
        this.counter = counter;
        this.maxConsumers = maxConsumers;
        this.globalMaxPrime = globalMaxPrime;
        this.globalMinPrime = globalMinPrime;
        this.activeConsumers = activeConsumers;
    }
/*    public void startConsumerExec() {

    }*/

    @Override
    public void run() {
        Thread.currentThread().setName("Consumer-" + id);

        try {
            while (true) {
                List<Integer> primeNums = new ArrayList<>();

                // respect pause before doing any work
                if (threadStopper.isPaused()) {
                    System.out.println(Thread.currentThread().getName() + " waiting (paused)...");
                }
                threadStopper.waitIfPaused();
                if (stopping) return;   // just in case someone called stop

                // block until we get one item
                String item = queue.take();   // no loop, no poll()

                if (STOP.equals(item)) {
                    System.out.println(Thread.currentThread().getName() + " got STOP, exiting.");
                    return;
                }

                List<Integer> nums = getNums(item);
                for (Integer num : nums) {
                    if (isPrime(num)) primeNums.add(num);
                }

                System.out.println(Thread.currentThread().getName() + " processed file");

                Counters counters = new Counters(item, fileCounter);
                String fileName = counters.getFileName();
                System.out.println("file: " + fileName);
                System.out.println("Consuming " + primeNums);
                System.out.println(primeNums.size() + " of " + nums.size());

                synchronized (primeCounts) {
                    primeCounts.add(primeNums.size());
                }

                int maxCount = getMaxPrimeCount(primeCounts);
                int maxPrime = getMaxPrimeCount(primeNums);
                int minPrime = getMinPrimeCount(primeNums);
                if (maxPrime > globalMaxPrime.get())
                    globalMaxPrime.set(maxPrime);
                if (minPrime < globalMinPrime.get())
                    globalMinPrime.set(minPrime);

                int threadNum = counter.incrementAndGet();
                counters.incrementFileCounter();
                int filesProcessed = counters.getFileCounter();

                System.out.println("max count: " + maxCount);
                System.out.println("max prime: " + maxPrime);

                MainPageController c = MainPage.controller;

                writer.writeResults("\nThread"+id);
                writer.writeResults("Filename: " + fileName);
                writer.writeResults("Number of primes: " + maxCount +
                        "\nMax prime: " + maxPrime);
                writer.writeResults("Prime Numbers: " + primeNums);

                if (c != null) {
                    c.showMax(id, primeNums.size(), nums.size(), fileName);
                    c.counter(globalMaxPrime.get(), globalMinPrime.get(),
                            maxCount, maxConsumers.get());
                    // ❌ REMOVE this line:
                    // c.setCurrentThreadLabel(resultIndex);

                    c.setFilesLabel(filesProcessed);
                } else {
                    System.out.println("Controller not ready yet");
                }

            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            // consumer is exiting for real
            activeConsumers.decrementAndGet();
            MainPageController c = MainPage.controller;
            if (c != null) {
                c.setCurrentThreadLabel(activeConsumers.get());
                c.removeThreadUI(id);
            }
        }
    }
}
