package project_331;

import java.util.Random;
import java.util.Scanner;

public class Driver {
    private static final Random random = new Random();
    private static final Scanner sc = new Scanner(System.in);
    private static final String BASE_LOG_FILE = "C:/Users/Salman/Desktop/log.txt";

    public static void main(String[] args) {
        System.out.print("Enter a temperature cap: ");
        double cap = sc.nextDouble();

        FileLogger logger = new FileLogger(BASE_LOG_FILE);
        double previousValid3 = 0;
        
        for (int cycle = 1; cycle <= 5; cycle++) {
            System.out.println("\n--- Cycle " + cycle + " ---");

            double temp = generateTemp(cap);
            System.out.printf("Temperature: %.2f\n", temp);

            double hum = generateHumidity();
            System.out.printf("Humidity: %.2f%%\n", hum);

            double s1 = generateSensor3();
            double s2 = generateSensor3();
            double s3 = generateSensor3();
            System.out.printf("Sensor3.1=%.0f, Sensor3.2=%.0f, Sensor3.3=%.0f\n", s1, s2, s3);

            
            double voted = majorityVote(s1, s2, s3);
            if (Double.isNaN(voted)) {
                logger.logEvent(String.format(
                    "No majority among [%.0f, %.0f, %.0f]; fallback to %.0f",
                    s1, s2, s3, previousValid3
                ));
                voted = previousValid3;
            } else {
                if (!(s1 == s2 && s2 == s3)) {
                    StringBuilder out = new StringBuilder();
                    if (s1 != voted) out.append("3.1 ");
                    if (s2 != voted) out.append("3.2 ");
                    if (s3 != voted) out.append("3.3 ");
                    logger.logEvent("Discrepancy detected; outlier(s): " + out.toString().trim());
                }
                previousValid3 = voted;
            }

            System.out.printf("Voted Sensor3 Value: %.0f\n", voted);
        }

        sc.close();
    }

    private static double majorityVote(double a, double b, double c) {
        if (a == b || a == c) return a;
        if (b == c) return b;
        return Double.NaN;
    }

    private static double generateSensor3() {
        return random.nextInt(10);
    }

    private static double generateTemp(double cap) {
        return random.nextDouble() * cap;
    }

    private static double generateHumidity() {
        return random.nextDouble() * 100;
    }
}

