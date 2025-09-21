package mod.fuji.core.auxiliary;

import java.util.UUID;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Random;

public class RandomUtil {

    private static final Random random = new Random();

    public static <T> T drawList(@NotNull List<T> list) {
        return list.get(random.nextInt(list.size()));
    }

    public static String randomUUID() {
        return UUID.randomUUID().toString();
    }

    @SuppressWarnings("DataFlowIssue")
    public static int getRandomNumberExclusive(int minInclusive, int maxExclusive) {
        if (minInclusive >= maxExclusive) {
            LogUtil.error("The min value {} must be less than the max value {}. Returning the minimal value as the random number.", minInclusive, maxExclusive);
            return Math.min(minInclusive, maxExclusive);
        }

        return random.nextInt(minInclusive, maxExclusive);
    }

    public static int getRandomNumber(int minInclusive, int maxInclusive) {
        return getRandomNumberExclusive(minInclusive, maxInclusive + 1);
    }
}
