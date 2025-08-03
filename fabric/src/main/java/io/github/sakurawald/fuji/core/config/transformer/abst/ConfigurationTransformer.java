package io.github.sakurawald.fuji.core.config.transformer.abst;

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
    All the transformers should work transparently.
    """)
@SuppressWarnings("LombokGetterMayBeUsed")
public abstract class ConfigurationTransformer {

    @Getter
    protected Path targetFilePath;

    public void configure(Path targetFilePath) {
        this.targetFilePath = targetFilePath;
    }

    public abstract void apply();

    public void logOperation(@NotNull String message, Object... args) {
        Object[] finalArgs = new Object[args.length + 1];
        finalArgs[0] = this.targetFilePath;
        System.arraycopy(args, 0, finalArgs, 1, args.length);

        LogUtil.warn("Apply the transformer installed on the file `{}`\n => Message: " + message, finalArgs);
    }
}
