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
            maxConsumers.incrementAndGet();
            activeConsumers.incrementAndGet();
            Thread.sleep(5);
        }

    }

    private void removeConsumers(int count) {
        for (int i = 0; i < count && !consumers.isEmpty(); i++) {
            MainPageController c = MainPage.controller;
            try {
                Thread t = consumers.get(consumers.size() - 1); // or any index you like
                consumers.remove(t);

                maxConsumers.decrementAndGet();

                queue.put(STOP);
                System.out.println("Retiring thread " + t.getName());

                String identifier = t.getName().split("-")[1];
                if (c != null) {
                    c.removeThreadUI(Integer.valueOf(identifier));
                }
                System.out.println(identifier + " removed");



            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public void adjustThreadsExecution(int selectedThreadAdjust, AtomicInteger maxConsumers, MainPageController c) throws InterruptedException {
        new Thread(() -> {

            int current = consumers.size();  // TRUE active count, maintained by startConsumerThread + ConsumerExecution.finally

            if (selectedThreadAdjust > current) {
                int diff = selectedThreadAdjust - current;
                try {
                    addThreads(diff, maxConsumers);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            } else if (selectedThreadAdjust < current) {
                int diff = current - selectedThreadAdjust;

                removeConsumers(diff);

            }


        }, "AdjustThreadWatcher").start();
    }
}
