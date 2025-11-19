package kakha.kudava.primecountermultithread.interactions;

import kakha.kudava.primecountermultithread.MainPage;
import kakha.kudava.primecountermultithread.PrimeProcessingManager;
import kakha.kudava.primecountermultithread.controller.MainPageController;

import java.util.concurrent.atomic.AtomicInteger;

import static kakha.kudava.primecountermultithread.PrimeProcessingManager.*;

public class ThreadAdjust {
    private void addThreads(int threadNum, AtomicInteger maxConsumers) throws InterruptedException {

        for (int i = 0; i < threadNum; i++) {
            PrimeProcessingManager.startConsumerThread();
            maxConsumers.addAndGet(1);
            Thread.sleep(100);
        }

    }

    private void removeConsumers(int count) {
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

    public void adjustThreadsExecution(int selectedThreadAdjust, AtomicInteger maxConsumers, MainPageController c) throws InterruptedException {
        new Thread(() -> {

            int currentThread = c.getCurrentThread();
            int currentThreadMax = maxConsumers.get();
            if(selectedThreadAdjust > currentThreadMax) {
                int diff = selectedThreadAdjust - currentThreadMax;
                try {
                    System.out.println(diff);
                    addThreads(diff, maxConsumers);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            if (selectedThreadAdjust < currentThreadMax) {
                int diff = currentThreadMax - selectedThreadAdjust;
                removeConsumers(diff);
            }



        }, "AdjustThreadWatcher").start();
    }
}
