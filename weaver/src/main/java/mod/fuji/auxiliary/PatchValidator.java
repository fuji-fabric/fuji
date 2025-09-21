package mod.fuji.auxiliary;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class PatchValidator {

    @SuppressWarnings("SameParameterValue")
    public static void withMinimalPatchCount(int minCount, Consumer<AtomicInteger> consumer) {
        withRangedPatchCount(minCount, Integer.MAX_VALUE, consumer);
    }

    @SuppressWarnings("SameParameterValue")
    public static void withRangedPatchCount(int minCount, int maxCount, Consumer<AtomicInteger> consumer) {
        AtomicInteger patchCount = new AtomicInteger(0);
        consumer.accept(patchCount);

        if (patchCount.get() < minCount) {
            throw new IllegalStateException("Patch count %d < %d".formatted(patchCount.get(), minCount));
        }

        if (patchCount.get() > maxCount) {
            throw new IllegalStateException("Patch count %d > %d".formatted(patchCount.get(), maxCount));
        }
    }
}
