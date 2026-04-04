package mod.fuji.core.structure;

import org.jetbrains.annotations.NotNull;

public interface ConsoleSpammer {

    boolean isConsoleSpammer();

    default void trySpamConsole(@NotNull Runnable runnable) {
        if (this.isConsoleSpammer()) {
            runnable.run();
        }
    }

}
