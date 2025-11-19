package kakha.kudava.primecountermultithread.services;

import java.util.concurrent.atomic.AtomicInteger;

public class Counters {
    private final String content;
    private AtomicInteger fileCounter = new AtomicInteger(0);
    public Counters(String content, AtomicInteger fileCounter) {
        this.content = content;
        this.fileCounter = fileCounter;
    }

    public String getFileName() {
        String[] split = content.split("\n");
        return split[0];
    }

    public int incrementFileCounter() {
        return fileCounter.incrementAndGet();
    }

    public int getFileCounter() {
        return fileCounter.get();
    }

    public String getContent() {
        return content;
    }


}
