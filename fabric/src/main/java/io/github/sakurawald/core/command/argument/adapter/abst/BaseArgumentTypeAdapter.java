package io.github.sakurawald.core.command.argument.adapter.abst;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.github.sakurawald.core.auxiliary.ReflectionUtil;
import io.github.sakurawald.core.command.argument.structure.Argument;
import io.github.sakurawald.core.manager.Managers;
import lombok.Getter;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public abstract class BaseArgumentTypeAdapter {

    // predefined only for test env
    private static final Map<String, Class<?>> predefined = new HashMap<>() {
        {
            this.put("str", String.class);
            this.put("int", int.class);
        }
    };
    private static final Map<String, Class<?>> string2class = new HashMap<>() {
        {
            this.putAll(predefined);
        }
    };

    @Getter
    private static final List<BaseArgumentTypeAdapter> adapters = new ArrayList<>();

    @SuppressWarnings("unchecked")
    public static void registerAdapters() {
        // the `/reload` command will trigger the command registration event.
        string2class.clear();

        ReflectionUtil.getGraph(ReflectionUtil.ARGUMENT_TYPE_ADAPTER_GRAPH_FILE_NAME)
            .stream()
            .filter(className -> Managers.getModuleManager().shouldWeEnableThis(className))
            .forEach(className -> {
                try {
                    /* make instance of type adapter */
                    Class<? extends BaseArgumentTypeAdapter> clazz = (Class<? extends BaseArgumentTypeAdapter>) Class.forName(className);
                    Constructor<? extends BaseArgumentTypeAdapter> constructor = clazz.getDeclaredConstructor();
                    BaseArgumentTypeAdapter adapter = constructor.newInstance();
                    adapters.add(adapter);

                    /* register type mapping */
                    Class<?> typeClass = adapter.getTypeClasses().getFirst();
                    adapter.getTypeStrings().forEach(typeString -> {
                        if (string2class.containsKey(typeString) && !predefined.containsKey(typeString)) {
                            throw new IllegalStateException("Type `%s` is already registered".formatted(typeString));
                        }
                        string2class.put(typeString, typeClass);
                    });

                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });

    }

    public static Class<?> toTypeClass(String typeString) {
        Class<?> type = string2class.get(typeString);
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
        for (BaseArgumentTypeAdapter adapter : adapters) {
            if (adapter.match(type)) {
                return adapter;
            }
        }

        throw new RuntimeException("No adapters match the argument type: " + type.getTypeName());
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

}
