package io.github.sakurawald.fuji.core.auxiliary;

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
}
