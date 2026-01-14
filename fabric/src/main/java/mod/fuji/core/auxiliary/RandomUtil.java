package mod.fuji.core.auxiliary;

import java.util.UUID;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Random;

public class RandomUtil {

    private static final Random RNG = new Random();

    public static <T> @NotNull T drawList(@NotNull List<T> list) {
        return list.get(RNG.nextInt(list.size()));
    }

    public static @NotNull String drawUUID() {
        return UUID.randomUUID().toString();
    }

    @SuppressWarnings("DataFlowIssue")
    public static int drawNumberExclusive(int minInclusive, int maxExclusive) {
        if (minInclusive >= maxExclusive) {
            LogUtil.error("The min value {} must be less than the max value {}. Returning the minimal value as the random number.", minInclusive, maxExclusive);
            return Math.min(minInclusive, maxExclusive);
        }

        return RNG.nextInt(minInclusive, maxExclusive);
    }

    public static int drawNumber(int minInclusive, int maxInclusive) {
        return drawNumberExclusive(minInclusive, maxInclusive + 1);
    }
}
