package kakha.kudava.primecountermultithread;

import java.util.ArrayList;
import java.util.List;
import java.util.Collections;

public class FileNums {
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

    static public int getMaxPrimeCount(List<Integer> primes) {

        if (primes == null || primes.isEmpty()) return 0; // avoid NoSuchElementException
        return Collections.max(primes);

    }
}
