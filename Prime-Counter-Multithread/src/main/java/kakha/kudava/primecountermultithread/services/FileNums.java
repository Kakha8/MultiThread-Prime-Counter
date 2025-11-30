package kakha.kudava.primecountermultithread.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Collections;

public class FileNums {
    public static boolean isPrime(int n) {
        if (n < 2) return false;
        if (n == 2 || n == 3) return true;
        if (n % 2 == 0 || n % 3 == 0) return false;
        for (int i = 5; (long)i * i <= n; i += 6) {
            if (n % i == 0 || n % (i + 2) == 0) return false;
        }
        return true;
    }

    public static List<Integer> getNums(String numbers) {
        String[] lines = numbers.split("\\R");  // split into lines
        List<Integer> nums = new ArrayList<>();

        // Start from line index 1, skip the first line
        for (int i = 1; i < lines.length; i++) {
            String line = lines[i];
            String[] splitNums = line.trim().split("\\s+");

            for (String num : splitNums) {
                if (num.isEmpty()) continue;
                if (!num.matches("-?\\d+")) continue;

                try {
                    nums.add(Integer.parseInt(num));
                } catch (NumberFormatException e) {
                    System.err.println("Skipping invalid number: " + num);
                }
            }
        }

        return nums;
    }

    static public int getMaxPrimeCount(List<Integer> primes) {

        if (primes == null || primes.isEmpty()) return 0; // avoid NoSuchElementException
        return Collections.max(primes);

    }

    static public int getMinPrimeCount(List<Integer> primes) {

        if (primes == null || primes.isEmpty()) return 0; // avoid NoSuchElementException
        return Collections.min(primes);

    }
}
