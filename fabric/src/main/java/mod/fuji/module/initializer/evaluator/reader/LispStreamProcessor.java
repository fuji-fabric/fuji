package mod.fuji.module.initializer.evaluator.reader;

import org.jetbrains.annotations.NotNull;

public abstract class LispStreamProcessor<T, U, R> {

    protected int start;
    protected int end;

    public LispStreamProcessor() {
        this.start = 0;
        this.end = 0;
    }

    protected abstract int streamLength();

    protected boolean hasNext() {
        return start < streamLength();
    }

    protected void forward() {
        end++;
    }

    protected abstract @NotNull T peek();

    protected abstract @NotNull T previous();

    protected boolean selectAny() {
        return start != end;
    }

    protected abstract void emit(@NotNull R r);

    protected void syncStart() {
        start = end;
    }

    @NotNull
    protected abstract U select();
}
