package io.github.sakurawald.core.structure.descriptor;

import io.github.sakurawald.core.auxiliary.ReflectionUtil;
import io.github.sakurawald.core.manager.impl.module.ModuleManager;
import io.github.sakurawald.core.structure.descriptor.interfaces.SourceModuleGetter;
import lombok.Data;
import net.minecraft.item.Item;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@Data
public abstract class StringDescriptor implements SourceModuleGetter {

    public static final List<StringDescriptor> REGISTERED_STRING_DESCRIPTORS = new ArrayList<>();

    private final String pattern;
    private String string;
    private final String document;
    private final String fromModule;

    private void compilePattern() {
        // We need the `pattern` for named variables, since we don't want to display the `%s` directly.
        this.string = pattern.replaceAll("<.*?>", "%s");
    }

    public StringDescriptor(@NotNull String pattern, @Nullable String document) {
        this(false, pattern, document);
    }

    public StringDescriptor(boolean temporary, @NotNull String pattern, @Nullable String document) {
        this.pattern = pattern;
        this.document = document;

        /* Set the source module. */
        this.fromModule = ReflectionUtil.findSourceModuleInCurrentStack();

        /* Compile the string pattern. */
        this.compilePattern();

        /* Register self. */
        tryRegister(temporary, fromModule);
    }

    private void tryRegister(boolean temporary, String sourceModule) {
        /* Should not register a temporary descriptor. */
        if (temporary) return;

        /* Should only register it when the module is enabled. */
        List<String> modulePathList = ModuleManager.splitModulePath(sourceModule);
        Boolean moduleEnableStatus = ModuleManager.MODULE_ENABLE_STATUS.getOrDefault(modulePathList, false);
        if (moduleEnableStatus) {
            REGISTERED_STRING_DESCRIPTORS.add(this);
        }
    }

    public int sortPriority() {
        return 0;
    }

    public abstract String getStringType();

    public abstract Item toItem();

    public String withArguments(Object... arguments) {
        if (arguments.length != 0) {
            return this.string.formatted(arguments);
        }

        return this.string;
    }

    @Override
    public String getSourceModule() {
        return this.fromModule;
    }
}
