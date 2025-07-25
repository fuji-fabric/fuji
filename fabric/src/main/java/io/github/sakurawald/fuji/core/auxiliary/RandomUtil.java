package io.github.sakurawald.fuji.core.auxiliary;

import java.util.UUID;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Random;

public class RandomUtil {

    @Getter
    private static final Random random = new Random();

    public static <T> T drawList(@NotNull List<T> list) {
        return list.get(random.nextInt(list.size()));
    }

    public static String randomUUID() {
        return UUID.randomUUID().toString();
    }

    @SuppressWarnings("DataFlowIssue")
    public static int getRandomInRange(int min, int max) {
        if (min > max) {
            LogUtil.error("The min value {} must be less than or equal to max value {}. (Returns the minimal value as the random number)", min, max);
            return Math.min(min, max);
        }
        return random.nextInt(min, max + 1);
    }
}
