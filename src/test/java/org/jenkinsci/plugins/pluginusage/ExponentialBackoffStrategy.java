package org.jenkinsci.plugins.pluginusage;

import java.util.Random;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class ExponentialBackoffStrategy {

    public static <T> void attempt(Supplier<T> action, Predicate<T> success, long maxTimeBackoffMillis) {
        int attempts = 0;

        T result = action.get();
        while (!success.test(result)) {
            try {
                Thread.sleep(getWaitTimeExp(attempts++, maxTimeBackoffMillis));
            } catch ( InterruptedException e ) {
                //handle exception
            }
            result = action.get();
        }
    }

    private static long getWaitTimeExp(int attempts, long maxTimeBackoffMillis) {
        final Random random = new Random();
        final double pow = Math.pow( 2, attempts);
        final int rand = random.nextInt( 1000 );
        return ( long ) Math.min( pow + rand, maxTimeBackoffMillis);
    }
}
