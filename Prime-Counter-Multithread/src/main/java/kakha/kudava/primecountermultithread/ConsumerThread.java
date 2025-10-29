package kakha.kudava.primecountermultithread;

import com.sun.tools.javac.Main;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingDeque;

public class ConsumerThread {

    static boolean isPrime(int n) {
        if (n < 2) return false;
        if (n == 2 || n == 3) return true;
        if (n % 2 == 0 || n % 3 == 0) return false;
        for (int i = 5; (long)i * i <= n; i += 6) {
            if (n % i == 0 || n % (i + 2) == 0) return false;
        }
        return true;
    }

    static public List<Integer> getNums(String numbers) {
        String[] splitNums = numbers.split("\\s+"); // split on any whitespace
        List<Integer> nums = new ArrayList<>();

        for (String num : splitNums) {
            num = num.trim(); // remove leading/trailing spaces
            if (num.isEmpty()) continue; // skip blanks
            if (!num.matches("-?\\d+")) continue; // skip anything not purely digits

            try {
                nums.add(Integer.parseInt(num));
            } catch (NumberFormatException e) {
                // skip numbers too large for int, or malformed ones
                System.err.println("Skipping invalid number: " + num);
            }
        }
        return nums;
    }

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

                        MainPageController c = MainPage.controller;
                        if (c != null) {
                            c.showMax(primeNums.size(), nums.size());  // showMax does Platform.runLater(...)
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

    public static void main(String[] args) throws IOException {

        //consumer.start();

/*        String content = Files.readString(Path.of("data-files/1.txt"));
        String[] lines = content.split("\n");
        for (String line : lines) {
            System.out.println(line);
        }*/
    }
}
