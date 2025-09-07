package io.github.sakurawald.fuji.core.command.processor;

import com.mojang.brigadier.CommandDispatcher;
import io.github.sakurawald.fuji.Fuji;
import io.github.sakurawald.fuji.core.annotation.Unused;
import io.github.sakurawald.fuji.core.auxiliary.LogUtil;
import io.github.sakurawald.fuji.core.auxiliary.ReflectionUtil;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.fuji.core.command.annotation.CommandArgName;
import io.github.sakurawald.fuji.core.command.annotation.CommandNode;
import io.github.sakurawald.fuji.core.command.annotation.CommandRequirement;
import io.github.sakurawald.fuji.core.command.argument.adapter.abst.BaseArgumentTypeAdapter;
import io.github.sakurawald.fuji.core.command.argument.structure.CommandArgument;
import io.github.sakurawald.fuji.core.command.config.model.PermissionModel;
import io.github.sakurawald.fuji.core.command.descriptor.CommandDescriptor;
import io.github.sakurawald.fuji.core.command.descriptor.RetargetCommandDescriptor;
import io.github.sakurawald.fuji.core.command.structure.CommandRequirementDescriptor;
import io.github.sakurawald.fuji.core.config.handler.abst.BaseConfigurationHandler;
import io.github.sakurawald.fuji.core.config.handler.impl.ObjectConfigurationHandler;
import io.github.sakurawald.fuji.core.document.annotation.Cite;
import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.core.document.annotation.TestCase;
import io.github.sakurawald.fuji.core.event.annotation.EventConsumer;
import io.github.sakurawald.fuji.core.event.message.server.command.OnCommandRegistrationEvent;
import io.github.sakurawald.fuji.core.manager.impl.module.ModuleManager;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import org.jetbrains.annotations.NotNull;

@Cite({
    "https://github.com/Revxrsal/Lamp"
    , "https://github.com/henkelmax/admiral"
})
@TestCase(action = "List the command tree of a normal user.", targets = "The command permissions should be handled properly.")
public class CommandAnnotationProcessor {

    public static final BaseConfigurationHandler<PermissionModel> permission = ObjectConfigurationHandler.ofPath(Fuji.MOD_CONFIG_PATH.resolve("permission.json"), PermissionModel.class);

    public static final Set<CommandDescriptor> REGISTERED_COMMAND_DESCRIPTORS = ConcurrentHashMap.newKeySet();
    public static final Set<String> LOADED_COMMAND_PATHS = new HashSet<>();
    public static final Set<String> PUBLIC_COMMAND_PATHS = new HashSet<>();

    public static CommandDispatcher<ServerCommandSource> COMMAND_DISPATCHER;
    public static CommandRegistryAccess COMMAND_REGISTRY_ACCESS;

    @EventConsumer(injectorPriority = EventConsumer.LOWEST, consumerPriority = EventConsumer.LOWEST)
    private static void setupCommandManagerReferences(OnCommandRegistrationEvent event) {
        /* Capture the variables. */
        CommandAnnotationProcessor.COMMAND_DISPATCHER = event.getDispatcher();
        CommandAnnotationProcessor.COMMAND_REGISTRY_ACCESS = event.getRegistryAccess();
    }

    @EventConsumer(injectorPriority = EventConsumer.HIGHEST, consumerPriority = EventConsumer.HIGHEST)
    private static void updateCommandTree(OnCommandRegistrationEvent event) {
        // NOTE: The `/reload` command invalidates the old CommandManager reference, here we have to capture the new reference to CommandManager.
        CommandManager commandManager = event.getCommandManager();
        CommandHelper.updateCommandTree(commandManager);
    }

    @EventConsumer
    private static void onCommandRegistrationEvent(@Unused OnCommandRegistrationEvent event) {
        // NOTE: The `/reload` command will clear all registered commands, and trigger the `REGISTRATION` event.
        /* Register argument type adapters. */
        BaseArgumentTypeAdapter.Registry.registerTypeAdapters();

        /* Read the latest permission file. */
        permission.readStorage();

        /* Register commands. */
        REGISTERED_COMMAND_DESCRIPTORS.clear();
        LOADED_COMMAND_PATHS.clear();
        PUBLIC_COMMAND_PATHS.clear();
        processClasses();

        /* Write the permission file back. */
        removePermissionMapOfUnloadedCommandPath();
        permission.writeStorage();
    }

    private static void removePermissionMapOfUnloadedCommandPath() {
        Map<String, Integer> permissionMap = permission
            .model()
            .getDefaultLevelPermission()
            .getCommands();

        permissionMap
            .keySet()
            .stream()
            .toList()
            .forEach(key -> {
                if (!LOADED_COMMAND_PATHS.contains(key)) {
                    LogUtil.warn("Removed unused permission map for command path '{}' in '{}' file.", key, permission.getFilePath());
                    permissionMap.remove(key);
                }
            });
    }

    private static void processClasses() {
        ModuleManager.MODULE_INITIALIZER_BY_CLASS.values()
            .stream()
            .filter(Objects::nonNull)
            .forEach(initializer -> processClass(initializer.getClass()));
    }

    private static void processClass(@NotNull Class<?> clazz) {
        ReflectionUtil
            .getMethodsWithAnnotation(clazz, CommandNode.class)
            .forEach(method -> processMethod(clazz, method));
    }

    private static void processMethod(@NotNull Class<?> clazz, @NotNull Method method) {
        /* Verify the method. */
        verifyMethod(clazz, method);

        /* Make the command descriptor from the method. */
        CommandDescriptor descriptor = makeCommandDescriptor(clazz, method);
        descriptor.register();

        /* Make the re-target command descriptor. */
        RetargetCommandDescriptor
            .from(descriptor)
            .ifPresent(CommandDescriptor::register);
    }

    private static void verifyMethod(@NotNull Class<?> clazz, @NotNull Method method) {
        /* Verify the return type of the command method. */
        if (!method.getReturnType().equals(int.class)) {
            throw new RuntimeException("The method `%s` in class `%s` must return the primitive int data type.".formatted(method.getName(), clazz.getName()));
        }

        /* Verify the static modifier of the command method. */
        if (!Modifier.isStatic(method.getModifiers())) {
            throw new RuntimeException("The method `%s` in class `%s` must be static.".formatted(method.getName(), clazz.getName()));
        }

    }

    private static @NotNull Class<?> unboxTypeClass(@NotNull Parameter parameter) {
        if (parameter.getType().equals(Optional.class)) {
            ParameterizedType parameterizedType = (ParameterizedType) parameter.getParameterizedType();
            return (Class<?>) parameterizedType.getActualTypeArguments()[0];
        }

        return parameter.getType();
    }

    private static @NotNull CommandDescriptor makeCommandDescriptor(@NotNull Class<?> clazz, @NotNull Method method) {
        List<CommandArgument> commandArgumentList = new ArrayList<>();

        /* Process the @CommandNode above the class. */
        CommandNode classAnnotation = clazz.getAnnotation(CommandNode.class);
        CommandRequirement classRequirement = clazz.getAnnotation(CommandRequirement.class);
        if (classAnnotation != null && !classAnnotation.value().isBlank()) {
            splitCommandNode(classAnnotation)
                .forEach(argumentName -> commandArgumentList.add(
                    CommandArgument.ofLiteralArgument(argumentName, CommandRequirementDescriptor.from(classRequirement))
                ));
        }

        /* Process the @CommandNode above the method. */
        method.setAccessible(true);
        CommandNode methodAnnotation = method.getAnnotation(CommandNode.class);
        if (methodAnnotation.topLevel()) {
            commandArgumentList.clear();
        }

        /* Push literal arguments. */
        CommandRequirement methodRequirement = null;
        for (String argumentName : splitCommandNode(methodAnnotation).toList()) {

            /* Pass the class requirement down, if the method requirement is null */
            methodRequirement = method.getAnnotation(CommandRequirement.class);
            if (methodRequirement == null) {
                methodRequirement = classRequirement;
            }

            /* Make requirement descriptor */
            commandArgumentList.add(CommandArgument
                .ofLiteralArgument(argumentName, CommandRequirementDescriptor.from(methodRequirement)));
        }

        /* Push required arguments. */
        Parameter[] parameters = method.getParameters();
        for (Parameter parameter : parameters) {
            /* Append the argument to the tail*/
            Class<?> typeClass = unboxTypeClass(parameter);
            boolean isOptional = parameter.getType().equals(Optional.class);
            CommandArgument commandArgument = CommandArgument
                .ofRequiredArgument(typeClass, getArgumentName(parameter), isOptional, CommandRequirementDescriptor.from(methodRequirement))
                .fillParameter(parameter);
            commandArgumentList.add(commandArgument);
        }

        /* Verify command descriptor. */
        verifyCommandDescriptor(clazz, method, commandArgumentList);

        /* Make the command descriptor. */
        CommandDescriptor commandDescriptor = new CommandDescriptor(method, commandArgumentList)
            .fillDocument(method.getAnnotation(Document.class));

        /* Apply the effective default command requirement. */
        CommandDescriptor.CommandRequirement.setEffectiveDefaultCommandRequirement(commandDescriptor);
        return commandDescriptor;
    }

    private static Stream<String> splitCommandNode(@NotNull CommandNode commandNodeAnnotation) {
        String[] split = commandNodeAnnotation.value().trim().split("\\s+");
        return Arrays
            .stream(split)
            // NOTE: https://errorprone.info/bugpattern/StringSplitter
            .filter(argumentName -> !argumentName.trim().isBlank());
    }

    private static @NotNull String getArgumentName(@NotNull Parameter parameter) {
        return Optional
            .ofNullable(parameter.getAnnotation(CommandArgName.class))
            .map(CommandArgName::value)
            .orElseGet(parameter::getName);
    }

    private static void verifyCommandDescriptor(@NotNull Class<?> clazz, @NotNull Method method, @NotNull List<CommandArgument> commandArgumentList) {
        /* A command descriptor must have at least 1 argument. */
        if (commandArgumentList.isEmpty()) {
            throw new RuntimeException("The argument list of @CommandNode annotated in method `%s` in class `%s` is empty.".formatted(method.getName(), clazz.getName()));
        }


        /* Verify the command argument types order. */
        boolean expectNonOptionalArgument = true;
        for (int i = 0; i < commandArgumentList.size(); i++) {
            CommandArgument commandArgument = commandArgumentList.get(i);

            /* The greedy string argument must be the last argument. */
            if (commandArgument.isGreedyArgumentType() && i != commandArgumentList.size() - 1) {
                throw new RuntimeException("The GreedyString argument type must be the last argument: class = %s, method = %s".formatted(clazz.getName(), method.getName()));
            }

            /* Check the order of non-optional arguments and optional arguments. */
            if (expectNonOptionalArgument) {
                if (commandArgument.isOptional()) {
                    expectNonOptionalArgument = false;
                }
            } else {
                if (!commandArgument.isOptional() && !commandArgument.isGreedyArgumentType()) {
                    throw new RuntimeException("The order of argument types must be: non-optional arguments, optional arguments and greedy string argument: class = %s, method = %s".formatted(clazz.getName(), method.getName()));
                }
            }

        }
    }

}
