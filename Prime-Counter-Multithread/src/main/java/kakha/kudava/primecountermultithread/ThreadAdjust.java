package kakha.kudava.primecountermultithread;

import java.util.concurrent.atomic.AtomicInteger;

import static kakha.kudava.primecountermultithread.ConsumerThread.*;

public class ThreadAdjust {
    public static void addThreads(int threadNum, AtomicInteger maxConsumers) throws InterruptedException {

        for (int i = 0; i < threadNum; i++) {
            ConsumerThread.startConsumerThread();
            maxConsumers.addAndGet(1);
            Thread.sleep(100);
        }

    }

    public static void removeConsumers(int count) {
        for (int i = 0; i < count && !consumers.isEmpty(); i++) {
            MainPageController c = MainPage.controller;
            try {
                Thread t = consumers.get(i);

                queue.put(STOP);   // retire one consumer
                System.out.println("Retiring thread " + t.getName());

                String identifier = t.getName().split("-")[1];
                c.removeThreadUI(Integer.valueOf(identifier));
                System.out.println(identifier + " removed");
                int currentThreadCount = counter.decrementAndGet();
                c.setCurrentThreadLabel(currentThreadCount);

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
