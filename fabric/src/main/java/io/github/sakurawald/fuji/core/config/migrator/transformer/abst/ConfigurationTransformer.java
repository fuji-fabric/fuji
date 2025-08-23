package io.github.sakurawald.fuji.core.config.migrator.transformer.abst;

import io.github.sakurawald.fuji.core.auxiliary.LogUtil;
import io.github.sakurawald.fuji.core.document.annotation.ForDeveloper;
import java.nio.file.Path;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

@ForDeveloper("""
    A `transformer` is used to `transform bits` in the `storage` or `memory`.
    Its typical use-case is to migrate data schema between versions.

    You can install multiple transformer instances on a specific `configuration handler`.
    Each transformer will be called before the call to the config model getter function.

    The rules to write a new configuration transformer:
    1. A transformer should work transparently, the bits should be transformed before the consumer sees it.
    2. You need to implement the apply() method properly:
    2.a. It should do necessary checks, including the source existence check and destination existence check.
    2.b. It should do nothing if there is no transformation needed, without the console-spam.
    """)
@SuppressWarnings("LombokGetterMayBeUsed")
public abstract class ConfigurationTransformer {

    @Getter
    protected Path targetFilePath;

    private void configure(@NotNull Path targetFilePath) {
        this.targetFilePath = targetFilePath;
    }

    public final void tryApply(@NotNull Path targetFilePath) {
        this.configure(targetFilePath);
        boolean canApply = this.canApply();
        LogUtil.debug("Can apply the transformer installed in file {}? -> {}", this.targetFilePath, canApply);

        if (canApply) {
            this.apply();
        }
    }

    protected abstract boolean canApply();

    protected abstract void apply();

    protected void logOperation(@NotNull String message, Object... args) {
        Object[] finalArgs = new Object[args.length + 1];
        finalArgs[0] = this.targetFilePath;
        System.arraycopy(args, 0, finalArgs, 1, args.length);

        LogUtil.warn("Apply the transformer installed on the file `{}`\n => " + message, finalArgs);
    }
}
