package kakha.kudava.primecountermultithread.executions;

import kakha.kudava.primecountermultithread.interactions.ThreadStopper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class ProducerExecution implements Runnable {

    private ThreadStopper threadStopper;
    private BlockingQueue<String> queue;
    private List<Thread> consumers;
    private String STOP;

    public ProducerExecution(ThreadStopper threadStopper, BlockingQueue<String> queue,
                             List<Thread> consumers, String STOP) {
        this.queue = queue;
        this.consumers = consumers;
        this.STOP = STOP;
        this.threadStopper = threadStopper;
    }


    @Override
    public void run() {
        try {
            String content = new String();

            for (int i = 1; i < 1001 + 1; i++) {
                System.out.println("Producing: " + i);

                content = "file" + String.valueOf(i) + ".txt" + "\n" +
                        Files.readString(Path.of("data-files/file" + String.valueOf(i) + ".txt"));

                // respecting pause between attempts
                while (true) {
                    threadStopper.waitIfPaused();
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
    }
}
