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

import static kakha.kudava.primecountermultithread.FileNums.*;

public class ConsumerThread {


    private static BlockingQueue<String> queue = new LinkedBlockingDeque<>(1);
    private static List<Thread> consumers = new CopyOnWriteArrayList<>();
    private static List<Integer> primeCounts = new ArrayList<Integer>();
    private static List<Boolean> threadCount = new ArrayList<Boolean>();
    private static final String STOP = "STOP";
    private static boolean stopping = false;

    private static Thread producer;

    public static void producerConsumer(int threadCountNum){

        //List<Thread> consumers = new CopyOnWriteArrayList<>();


        producer = new Thread(() -> {
            try {
                String content = new String();

                for (int i = 1; i < threadCountNum + 1; i++) {
                    System.out.println("Producing: " + i);

                    content = Files.readString(Path.of("data-files/file" + String.valueOf(i) + ".txt"));

                    queue.put(content); //wait till que is full
                    Thread.sleep(100);
                }

                queue.put("STOP");

            } catch (InterruptedException | IOException e){
                Thread.currentThread().interrupt();
            }
        });

        for (int i = 1; i <= threadCountNum + 1; i++) {
            // Consumer thread
            Thread consumer = new Thread(() -> {
                List<Integer> primeNums = new ArrayList<Integer>();

                try {
                    while (true) {
                        String item = queue.take(); // waits if queue is empty

                        if ("STOP".equals(item)) break;   // stop condition

                        List<Integer> nums = getNums(item);
                        for (Integer num : nums) {
                            if (isPrime(num)) primeNums.add(num);
                        }

                        System.out.println("Consuming " + primeNums);
                        System.out.println(primeNums.size() + " of " + nums.size());

                        primeCounts.add(primeNums.size());

                        int maxCount = getMaxPrimeCount(primeCounts);
                        int maxPrime = getMaxPrimeCount(primeNums);
                        int thread = consumers.size();

                        threadCount.add(true);

                        System.out.println("max count: " + String.valueOf(getMaxPrimeCount(primeCounts)));
                        System.out.println("max prime: " + String.valueOf(getMaxPrimeCount(primeNums)));



                        MainPageController c = MainPage.controller;
                        if (c != null) {
                            c.showMax(primeNums.size(), nums.size());
                            c.counter(maxPrime, maxCount, threadCount.size());
                            c.setCurrentThreadLabel(threadCount.size());
                        } else {
                            System.out.println("Controller not ready yet");
                        }


                        primeNums.clear();


                        Thread.sleep(200);
                    }

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
            consumer.start();
            consumers.add(consumer);

        }
        producer.start();
    }

    public static void stopThreads() {
        stopping = true;

        // Stop the producer
        if (producer != null && producer.isAlive()) {
            producer.interrupt();
        }

        // Clear any pending work
        queue.clear();

        // Wake every consumer with a poison pill
        int n = consumers.size();
        for (int i = 0; i < n; i++) {
            // offer to avoid blocking if queue gets momentarily full
            boolean offered = false;
            for (int tries = 0; tries < 3 && !offered; tries++) {
                try {
                    offered = queue.offer(STOP, 100, TimeUnit.MILLISECONDS);

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }

        //clear threads
        consumers.clear();
        producer.interrupt();
        queue.clear();

        // interrupt consumers in case any are sleeping
        for (Thread t : consumers) {
            if (t.isAlive()) t.interrupt();
        }

        // wait a bit for clean exit
        for (Thread t : consumers) {
            try {
                t.join(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    public static void adjustThreads(int threadCount) {
        MainPageController c = MainPage.controller;
        if (c == null) return;

        new Thread(() -> {
            while (true) {
                int currentThread = c.getCurrentThread();

                if (currentThread >= threadCount) {
                    System.out.println("Max thread reached, stopping threads...");
                    stopThreads();
                    c.disableStopBtn();
                    c.enableStartBtn();

                    break;
                }

                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    break;
                }
            }
        }, "AdjustThreadWatcher").start();
    }

}
