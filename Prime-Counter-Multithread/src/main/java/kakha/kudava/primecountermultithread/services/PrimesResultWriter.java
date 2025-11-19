package kakha.kudava.primecountermultithread.services;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import static java.awt.SystemColor.text;

public class PrimesResultWriter {

    private String filePath = "result\\primes-results.txt";

    public void writeResults(String text){
        try {
            Files.write(
                    Path.of(filePath),
                    (text + System.lineSeparator()).getBytes(StandardCharsets.UTF_8),
                    StandardOpenOption.CREATE,   // create file if it doesn't exist
                    StandardOpenOption.APPEND    // append to the end
            );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Boolean clearFile() {
        Path path = Path.of(filePath);

        // file does not exist → return false
        if (!Files.exists(path)) {
            return false;
        }

        try {
            Files.write(
                    path,
                    new byte[0],                       // empty content
                    StandardOpenOption.TRUNCATE_EXISTING
            );
            return true;   // cleared successfully

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
