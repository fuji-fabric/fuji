package io.github.sakurawald.core.auxiliary;

import lombok.Getter;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Random;

@UtilityClass
public class RandomUtil {

    @Getter
    private static final Random random = new Random();

    public static <T> T drawList(@NotNull List<T> list) {
        return list.get(random.nextInt(list.size()));
    }
}
