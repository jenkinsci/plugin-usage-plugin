package org.jenkinsci.plugins.pluginusage;

import java.time.Duration;
import java.time.Instant;
import java.util.Random;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class ExponentialBackoffStrategy {

    public static <T> void attempt(Supplier<T> action, Predicate<T> success, long maxTimeBackoffMillis) {
        int attempts = 0;

        Instant start = Instant.now();

        T result = action.get();
        while (!success.test(result)) {
            Instant now = Instant.now();
            if (Duration.between(start, now).compareTo(Duration.ofMillis(maxTimeBackoffMillis)) > 0){
                throw new RuntimeException("Maximum time has elapsed, aborting");
            }
            try {
                final long waitTimeExp = getWaitTimeExp(attempts++, maxTimeBackoffMillis);
                Thread.sleep(waitTimeExp);
            } catch ( InterruptedException e ) {
                //handle exception
            }
            result = action.get();
        }
    }

    public static <T> void attempt(String message, Supplier<T> action, Predicate<T> success, long maxTimeBackoffMillis) {
        System.out.println(message + " ⏳");
        attempt(action, success, maxTimeBackoffMillis);
        System.out.println(message + " ✅");
    }

    private static long getWaitTimeExp(int attempts, long maxTimeBackoffMillis) {
        final Random random = new Random();
        final double pow = Math.pow( 2, attempts);
        final int rand = random.nextInt( 1000 );
        return ( long ) Math.min( pow + rand, maxTimeBackoffMillis);
    }
}
