package kakha.kudava.primecountermultithread;

import static kakha.kudava.primecountermultithread.ConsumerThread.*;

public class ThreadAdjust {
    public static void addThreads(int threadNum){

        for (int i = 0; i < threadNum; i++) {
            ConsumerThread.startConsumerThread();
        }

    }

    public static void removeConsumers(int count) {
        for (int i = 0; i < count && !consumers.isEmpty(); i++) {
            try {
                queue.put(STOP);   // retire one consumer
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
