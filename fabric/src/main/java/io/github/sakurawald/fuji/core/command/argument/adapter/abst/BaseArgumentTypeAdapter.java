package io.github.sakurawald.fuji.core.command.argument.adapter.abst;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.github.sakurawald.fuji.core.auxiliary.LogUtil;
import io.github.sakurawald.fuji.core.auxiliary.ReflectionUtil;
import io.github.sakurawald.fuji.core.command.argument.structure.Argument;
import io.github.sakurawald.fuji.core.manager.Managers;
import io.github.sakurawald.fuji.core.manager.impl.module.ModuleManager;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public abstract class BaseArgumentTypeAdapter {

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
    public static void registerAdapters() {
        // the `/reload` command will trigger the command registration event.
        TYPE_STRING_2_TYPE_CLASS.clear();

        ReflectionUtil.CompileTimeGraph.getCompileTimeGraph(ReflectionUtil.CompileTimeGraph.ARGUMENT_TYPE_ADAPTER_GRAPH_FILE_NAME)
            .stream()
            .filter(className -> Managers.getModuleManager().shouldWeLoadThis(className))
            .forEach(className -> {
                try {
                    /* make instance of type adapter */
                    Class<? extends BaseArgumentTypeAdapter> clazz = (Class<? extends BaseArgumentTypeAdapter>) Class.forName(className);
                    Constructor<? extends BaseArgumentTypeAdapter> constructor = clazz.getDeclaredConstructor();
                    BaseArgumentTypeAdapter adapter = constructor.newInstance();
                    REGISTERED_COMMAND_ARGUMENT_TYPE_ADAPTERS.add(adapter);

                    /* register type mapping */
                    Class<?> typeClass = adapter.getTypeClasses().get(0);
                    adapter.getTypeStrings().forEach(typeString -> {
                        if (TYPE_STRING_2_TYPE_CLASS.containsKey(typeString) && !PREDEFINED_ARGUMENT_TYPES.containsKey(typeString)) {
                            throw new IllegalStateException("Type `%s` is already registered".formatted(typeString));
                        }
                        TYPE_STRING_2_TYPE_CLASS.put(typeString, typeClass);
                    });

                } catch (Exception e) {
                    LogUtil.error("Failed to register argument type adapter: className = {}", className, e);
                    throw new RuntimeException(e);
                }
            });

    }

    public static Class<?> toTypeClass(String typeString) {
        Class<?> type = TYPE_STRING_2_TYPE_CLASS.get(typeString);
        if (type == null)
            throw new IllegalArgumentException("Unknown argument type `%s`".formatted(typeString));

        return type;
    }

    private static Object box(Argument argument, Object value) {
        // pack the type
        if (argument.isOptional()) {
            return Optional.of(value);
        }

        return value;
    }

    public static BaseArgumentTypeAdapter getAdapter(Class<?> type) {
        for (BaseArgumentTypeAdapter adapter : REGISTERED_COMMAND_ARGUMENT_TYPE_ADAPTERS) {
            if (adapter.match(type)) {
                return adapter;
            }
        }

        throw new RuntimeException("No adapters match the argument type: " + type.getTypeName());
    }

    public @NotNull String getFromModule() {
        return ModuleManager.computeJoinedModulePath(this.getClass().getName());
    }

    private boolean match(Class<?> clazz) {
        return this.getTypeClasses().stream().anyMatch(it -> it.equals(clazz));
    }

    public RequiredArgumentBuilder<ServerCommandSource, ?> makeRequiredArgumentBuilder(String argumentName) {
        ArgumentType<?> argumentType = this.makeArgumentType();
        return CommandManager.argument(argumentName, argumentType);
    }

    protected abstract ArgumentType<?> makeArgumentType();

    protected abstract Object makeArgumentObject(CommandContext<ServerCommandSource> context, Argument argument);

    public abstract List<Class<?>> getTypeClasses();

    public abstract List<String> getTypeStrings();

    public final Object makeParameterObject(CommandContext<ServerCommandSource> ctx, Argument argument) {
        Object argumentObject = this.makeArgumentObject(ctx, argument);
        return box(argument, argumentObject);
    }

    public boolean verifyCommandSource(CommandContext<ServerCommandSource> context) {
        return true;
    }

    public boolean markAsVanillaMinecraftArgumentType() {
        return false;
    }

}
