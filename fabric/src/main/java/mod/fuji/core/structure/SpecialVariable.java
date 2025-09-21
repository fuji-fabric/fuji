package mod.fuji.core.structure;

import org.jetbrains.annotations.NotNull;

public class SpecialVariable<T> {

    @SuppressWarnings("ThreadLocalUsage")
    private final ThreadLocal<T> value = new ThreadLocal<>();
    private final T defaultValue;

    public SpecialVariable(@NotNull T defaultValue) {
        this.defaultValue = defaultValue;
    }

    public void bind(@NotNull T newValue, @NotNull Runnable body) {
        T oldValue = this.value.get();
        this.value.set(newValue);
        try {
            body.run();
        } finally {
            this.value.set(oldValue);
        }
    }

    public T get() {
        if (this.value.get() == null) {
            this.value.set(this.defaultValue);
        }
        return this.value.get();
    }
}
