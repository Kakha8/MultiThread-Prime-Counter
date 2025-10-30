package kakha.kudava.primecountermultithread;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingDeque;

import static kakha.kudava.primecountermultithread.FileNums.*;

public class ConsumerThread {



    public static void producerConsumer(){
        BlockingQueue<String> queue = new LinkedBlockingDeque<>(1);
        List<Thread> consumers = new CopyOnWriteArrayList<>();
        List<Integer> primeCounts = new ArrayList<Integer>();

        Thread producer = new Thread(() -> {
            try {
                String content = new String();

                for (int i = 1; i < 20; i++) {
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

        for (int i = 1; i < 30; i++) {
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

                        System.out.println("max count: " + String.valueOf(getMaxPrimeCount(primeCounts)));
                        System.out.println("max prime: " + String.valueOf(getMaxPrimeCount(primeNums)));


                        MainPageController c = MainPage.controller;
                        if (c != null) {
                            c.showMax(primeNums.size(), nums.size());
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


}
