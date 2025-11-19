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

    public ConsumerExecution(ThreadStopper threadStopper, int id, String STOP, boolean stopping,
                             BlockingQueue<String> queue, AtomicInteger fileCounter,
                             List<Integer> primeCounts, AtomicInteger counter) {
        this.threadStopper = threadStopper;
        this.id = id;
        this.STOP = STOP;
        this.stopping = stopping;
        this.queue = queue;
        this.fileCounter = fileCounter;
        this.primeCounts = primeCounts;
        this.counter = counter;

    }
/*    public void startConsumerExec() {

    }*/

    @Override
    public void run() {
        Thread.currentThread().setName("Consumer-" + id);
        List<Integer> primeNums = new ArrayList<>();

        try {
            // respect pause before doing any work
            if (threadStopper.isPaused()) {
                System.out.println(Thread.currentThread().getName() + " waiting (paused)...");
            }
            threadStopper.waitIfPaused();
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

/*
                synchronized (threadCount) {
                    threadCount.add(true);   // now: one entry per thread
                }
*/
            int threadNum = counter.incrementAndGet();
            counters.incrementFileCounter();
            int filesProcessed = counters.getFileCounter();

            System.out.println("max count: " + maxCount);
            System.out.println("max prime: " + maxPrime);

            MainPageController c = MainPage.controller;

            writer.writeResults("\nFilename: " + fileName);
            writer.writeResults("Number of primes: " + maxCount +
                    "\nMax prime: " + maxPrime);
            writer.writeResults("Prime Numbers: " + primeNums);

            if (c != null) {
                c.showMax(id, primeNums.size(), nums.size(), fileName);
                c.counter(maxPrime, maxCount, threadNum);
                c.setCurrentThreadLabel(threadNum);
                c.setFilesLabel(filesProcessed);
            } else {
                System.out.println("Controller not ready yet");
            }

            // thread ends here — no while loop, no extra files
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
