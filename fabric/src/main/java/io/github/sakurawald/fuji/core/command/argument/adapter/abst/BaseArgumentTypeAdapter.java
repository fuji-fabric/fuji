package io.github.sakurawald.fuji.core.command.argument.adapter.abst;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.github.sakurawald.fuji.core.auxiliary.LogUtil;
import io.github.sakurawald.fuji.core.auxiliary.ReflectionUtil;
import io.github.sakurawald.fuji.core.command.argument.structure.CommandArgument;
import io.github.sakurawald.fuji.core.command.assistant.CommandAssistant;
import io.github.sakurawald.fuji.core.command.suggestion.structure.ComposedCommandSuggestionsProvider;
import io.github.sakurawald.fuji.core.config.Configs;
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
    An `ArgumentTypeAdapter` is used to map a specific `ArgumentType` into its `ArgumentValue`.
    """)
public abstract class BaseArgumentTypeAdapter implements SourceModuleGetter {

    public static class Registry {

        public static final List<BaseArgumentTypeAdapter> REGISTERED_COMMAND_ARGUMENT_TYPE_ADAPTERS = new ArrayList<>();
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

        @SuppressWarnings({"unchecked"})
        public static void registerTypeAdapters() {
            // NOTE: The `/reload` command will trigger the command registration event.
            REGISTERED_COMMAND_ARGUMENT_TYPE_ADAPTERS.clear();
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
                            .getTypeNames()
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
            Class<?> typeClass = TYPE_STRING_2_TYPE_CLASS.get(typeString);
            if (typeClass == null) {
                throw new IllegalArgumentException("Unknown argument string '%s'".formatted(typeString));
            }

            return typeClass;
        }

        public static @NotNull BaseArgumentTypeAdapter getTypeAdapter(@NotNull Class<?> typeClass) {
            for (BaseArgumentTypeAdapter adapter : REGISTERED_COMMAND_ARGUMENT_TYPE_ADAPTERS) {
                if (adapter.supportsTypeClass(typeClass)) {
                    return adapter;
                }
            }

            throw new RuntimeException("No type adapter supports the type class: " + typeClass.getTypeName());
        }
    }

    private boolean supportsTypeClass(@NotNull Class<?> typeClass) {
        return this.getTypeClasses()
            .stream()
            .anyMatch(it -> it.equals(typeClass));
    }

    @ForDeveloper("This function returns a list of classes, to handle a specific type and all of its wrapper types.")
    public abstract List<Class<?>> getTypeClasses();

    @ForDeveloper("Allow to refer to an adapter using formal name or shortcut name.")
    public abstract List<String> getTypeNames();

    protected abstract ArgumentType<?> makeArgumentType();

    protected abstract Object makeArgumentValue(@NotNull CommandContext<ServerCommandSource> context, @NotNull CommandArgument commandArgument);

    public final @NotNull RequiredArgumentBuilder<ServerCommandSource, ?> makeComposedRequiredArgumentBuilder(@NotNull String argumentName) {
        RequiredArgumentBuilder<ServerCommandSource, ?> result = makeRequiredArgumentBuilder(argumentName);

        /* Command assistant feature. */
        if (Configs.MAIN_CONTROL_CONFIG.model().core.command.assistant.enable) {
            ComposedCommandSuggestionsProvider composedCommandSuggestionsProvider = new ComposedCommandSuggestionsProvider(result.getType(), result.getSuggestionsProvider(), CommandAssistant::assist);
            result.suggests(composedCommandSuggestionsProvider);
        }
        return result;
    }

    @NotNull
    protected RequiredArgumentBuilder<ServerCommandSource, ?> makeRequiredArgumentBuilder(@NotNull String argumentName) {
        ArgumentType<?> argumentType = this.makeArgumentType();
        return CommandManager.argument(argumentName, argumentType);
    }

    public final @NotNull Object makeParameterValue(@NotNull CommandContext<ServerCommandSource> context, @NotNull CommandArgument commandArgument) {
        Object argumentValue = this.makeArgumentValue(context, commandArgument);
        if (commandArgument.isOptional()) {
            return Optional.of(argumentValue);
        }

        return argumentValue;
    }

    @ForDeveloper("This method is used for an @CommandSource type adapter.")
    public boolean verifyCommandSource(@NotNull CommandContext<ServerCommandSource> context) {
        return true;
    }

    @ForDeveloper("This information is used in the in-game inspector.")
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
