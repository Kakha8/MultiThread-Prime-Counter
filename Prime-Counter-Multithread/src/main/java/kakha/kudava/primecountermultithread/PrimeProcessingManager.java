package kakha.kudava.primecountermultithread;

import kakha.kudava.primecountermultithread.controller.MainPageController;
import kakha.kudava.primecountermultithread.executions.ConsumerExecution;
import kakha.kudava.primecountermultithread.executions.ProducerExecution;
import kakha.kudava.primecountermultithread.interactions.ThreadAdjust;
import kakha.kudava.primecountermultithread.interactions.ThreadStopper;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicInteger;

public class PrimeProcessingManager {


    public static BlockingQueue<String> queue = new LinkedBlockingDeque<>(1000);
    public static List<Thread> consumers = new CopyOnWriteArrayList<>();
    private static List<Integer> primeCounts = new ArrayList<Integer>();
    //private static List<Boolean> threadCount = new ArrayList<Boolean>();
    public static AtomicInteger counter = new AtomicInteger(0);
    private static AtomicInteger threadId = new AtomicInteger(1);
    public static final String STOP = "STOP";

    public static AtomicInteger maxConsumers = new AtomicInteger(0);
    private static final Object PAUSE_LOCK = new Object();
    private static volatile boolean paused = false;
    private static boolean stopping = false;
    public static AtomicInteger fileCounter = new AtomicInteger(0);

    private static Thread producer;

    private static ThreadStopper threadStopper = new ThreadStopper(stopping, PAUSE_LOCK, paused,
            producer, consumers, queue, STOP, primeCounts, counter, threadId, maxConsumers);


    public static ThreadStopper getThreadStopper() {
        return threadStopper;
    }
    public static void startConsumerThread() {

        int id = threadId.getAndIncrement();
        ConsumerExecution execution = new ConsumerExecution(threadStopper, id, STOP, stopping,
                queue, fileCounter, primeCounts, counter);
        Thread consumer = new Thread(() -> {
            execution.run();
        });

        consumer.start();
        consumers.add(consumer);
    }



    public static void producerConsumer(int threadCountNum) throws InterruptedException {

        stopping = false;
        maxConsumers.set(threadCountNum);
        //System.out.println(maxConsumers);
        ProducerExecution execution = new ProducerExecution(threadStopper, queue, consumers, STOP);
        producer = new Thread(execution::run);

        System.out.println("threadcountnum: " + threadCountNum);
        for (int i = 1; i < threadCountNum + 1; i++){
            startConsumerThread();
        }

        // tells ThreadStopper which producer thread to manage
        threadStopper.setProducer(producer);

        producer.start();
    }
    public static void stopThreads() throws InterruptedException {
        System.out.println("Stopping threads...");
        threadStopper.stopThreadsExec();

    }

    public static void adjustThreads(int selectedThreadAdjust) throws InterruptedException {
        MainPageController c = MainPage.controller;
        if (c == null) return;
        ThreadAdjust threadAdjust = new ThreadAdjust();
        threadAdjust.adjustThreadsExecution(selectedThreadAdjust, maxConsumers, c);

    }

}
