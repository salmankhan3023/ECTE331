package project_331;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

public class FileLogger {
    public static final int MAX_BACKUP = 3;

    private final String baseName;
    private final Random random = new Random();
    private final DateTimeFormatter fmt = 
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    public FileLogger(String baseName) {
        this.baseName = baseName;
    }
    public void logEvent(String message) {
        String timestamp = fmt.format(LocalDateTime.now());
        String entry = timestamp + " - " + message;
        
        if (attemptLog(baseName, entry)) return;

        String prefix = baseName.replaceAll("\\.txt$", "");
        for (int i = 1; i <= MAX_BACKUP; i++) {
            String fn = prefix + i + ".txt";
            if (attemptLog(fn, entry)) return;
        }

     
        attemptLog("principal_log.txt", entry);
    }

    private boolean attemptLog(String filename, String entry) {
        try {
            if (random.nextInt(100) < 40) {
                throw new IOException("Simulated write failure");
            }
            try (FileWriter fw = new FileWriter(filename, true)) {
                fw.write(entry);
                fw.write(System.lineSeparator());
            }
            return true;
        } catch (IOException ex) {
            return false;
        }
    }
}
