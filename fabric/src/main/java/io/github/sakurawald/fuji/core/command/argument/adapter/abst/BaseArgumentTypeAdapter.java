package io.github.sakurawald.fuji.core.command.argument.adapter.abst;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.github.sakurawald.fuji.core.auxiliary.LogUtil;
import io.github.sakurawald.fuji.core.auxiliary.ReflectionUtil;
import io.github.sakurawald.fuji.core.command.argument.structure.Argument;
import io.github.sakurawald.fuji.core.document.annotation.ForDeveloper;
import io.github.sakurawald.fuji.core.document.interfaces.SourceModuleGetter;
import io.github.sakurawald.fuji.core.manager.Managers;
import io.github.sakurawald.fuji.core.manager.impl.module.ModuleManager;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import org.jetbrains.annotations.NotNull;

@ForDeveloper("""
    An `argument type adapter` is used to map a specific `ArgumentType` into its `Java Object instances`.
    """)
public abstract class BaseArgumentTypeAdapter implements SourceModuleGetter {

    // NOTE: Pre-define these argument types to make the fabric test environment happy.
    private static final Map<String, Class<?>> PREDEFINED_ARGUMENT_TYPES = new HashMap<>() {
        {
            this.put("str", String.class);
            this.put("int", int.class);
        }
    };

    private static final Map<String, Class<?>> TYPE_STRING_2_TYPE_CLASS = new HashMap<>() {
        {
            this.putAll(PREDEFINED_ARGUMENT_TYPES);
        }
    };

    public static final List<BaseArgumentTypeAdapter> REGISTERED_COMMAND_ARGUMENT_TYPE_ADAPTERS = new ArrayList<>();

    @SuppressWarnings({"unchecked"})
    public static void registerTypeAdapters() {
        // NOTE: The `/reload` command will trigger the command registration event.
        TYPE_STRING_2_TYPE_CLASS.clear();

        ReflectionUtil.CompileTimeGraph.getCompileTimeGraph(ReflectionUtil.CompileTimeGraph.ARGUMENT_TYPE_ADAPTER_GRAPH_FILE_NAME)
            .stream()
            .filter(className -> Managers.getModuleManager().shouldWeLoadThis(className))
            .forEach(className -> {
                try {
                    /* Make the instance of type adapter */
                    Class<? extends BaseArgumentTypeAdapter> adapterClass = (Class<? extends BaseArgumentTypeAdapter>) Class.forName(className);
                    Constructor<? extends BaseArgumentTypeAdapter> adapterConstructor = adapterClass.getDeclaredConstructor();
                    BaseArgumentTypeAdapter adapterInstance = adapterConstructor.newInstance();
                    REGISTERED_COMMAND_ARGUMENT_TYPE_ADAPTERS.add(adapterInstance);

                    /* Register type mapping. */
                    Class<?> adaptedTypeClass = adapterInstance
                        .getTypeClasses()
                        .get(0);
                    adapterInstance
                        .getTypeStrings()
                        .forEach(typeString -> {
                            if (TYPE_STRING_2_TYPE_CLASS.containsKey(typeString) && !PREDEFINED_ARGUMENT_TYPES.containsKey(typeString)) {
                                throw new IllegalStateException("Type string `%s` is already registered.".formatted(typeString));
                            }
                            TYPE_STRING_2_TYPE_CLASS.put(typeString, adaptedTypeClass);
                        });

                } catch (Exception e) {
                    LogUtil.error("Failed to register an argument type adapter: className = {}", className, e);
                    throw new RuntimeException(e);
                }
            });

    }

    public static @NotNull Class<?> toTypeClass(@NotNull String typeString) {
        Class<?> type = TYPE_STRING_2_TYPE_CLASS.get(typeString);
        if (type == null) {
            throw new IllegalArgumentException("Unknown argument type `%s`".formatted(typeString));
        }

        return type;
    }

    private static @NotNull Object box(@NotNull Argument argument, @NotNull Object value) {
        // pack the type
        if (argument.isOptional()) {
            return Optional.of(value);
        }

        return value;
    }

    public static @NotNull BaseArgumentTypeAdapter getAdapter(@NotNull Class<?> type) {
        for (BaseArgumentTypeAdapter adapter : REGISTERED_COMMAND_ARGUMENT_TYPE_ADAPTERS) {
            if (adapter.match(type)) {
                return adapter;
            }
        }

        throw new RuntimeException("No adapters match the argument type: " + type.getTypeName());
    }

    private boolean match(Class<?> clazz) {
        return this.getTypeClasses().stream().anyMatch(it -> it.equals(clazz));
    }

    public @NotNull RequiredArgumentBuilder<ServerCommandSource, ?> makeRequiredArgumentBuilder(@NotNull String argumentName) {
        ArgumentType<?> argumentType = this.makeArgumentType();
        return CommandManager.argument(argumentName, argumentType);
    }

    protected abstract ArgumentType<?> makeArgumentType();

    protected abstract Object makeArgumentObject(@NotNull CommandContext<ServerCommandSource> context, @NotNull Argument argument);

    @ForDeveloper("This function returns a list of classes, to handle a specific type and all of its wrapper types.")
    public abstract List<Class<?>> getTypeClasses();

    @ForDeveloper("Allow to refer to an adapter using formal name or shortcut name.")
    public abstract List<String> getTypeStrings();

    public final @NotNull Object makeParameterObject(@NotNull CommandContext<ServerCommandSource> ctx, @NotNull Argument argument) {
        Object argumentObject = this.makeArgumentObject(ctx, argument);
        return box(argument, argumentObject);
    }

    public boolean verifyCommandSource(@NotNull CommandContext<ServerCommandSource> context) {
        return true;
    }

    public boolean isVanillaMinecraftArgumentType() {
        return getTypeClasses()
            .stream()
            .anyMatch(argumentClass -> {
                String className = argumentClass.getName();
                return className.startsWith("net.minecraft")
                    || className.startsWith("com.mojang")
                    || className.startsWith("java.lang");
            });
    }

    @Override
    public @NotNull String getSourceModule() {
        return ModuleManager.computeJoinedModulePath(this.getClass().getName());
    }

}
