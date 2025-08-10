package io.github.sakurawald.fuji.core.command.processor;

import com.mojang.brigadier.CommandDispatcher;
import io.github.sakurawald.fuji.core.command.argument.structure.CommandArgument;
import io.github.sakurawald.fuji.core.document.annotation.Cite;
import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.core.auxiliary.ReflectionUtil;
import io.github.sakurawald.fuji.core.command.annotation.CommandNode;
import io.github.sakurawald.fuji.core.command.annotation.CommandRequirement;
import io.github.sakurawald.fuji.core.command.annotation.CommandSource;
import io.github.sakurawald.fuji.core.command.argument.adapter.abst.BaseArgumentTypeAdapter;
import io.github.sakurawald.fuji.core.command.structure.CommandDescriptor;
import io.github.sakurawald.fuji.core.command.structure.CommandRequirementDescriptor;
import io.github.sakurawald.fuji.core.command.structure.RetargetCommandDescriptor;
import io.github.sakurawald.fuji.core.document.annotation.TestCase;
import io.github.sakurawald.fuji.core.event.impl.CommandEvents;
import io.github.sakurawald.fuji.core.manager.impl.module.ModuleManager;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.ServerCommandSource;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Cite({
    "https://github.com/Revxrsal/Lamp"
    , "https://github.com/henkelmax/admiral"
})
@TestCase(action = "List the command tree of a normal user.", targets = "The command permissions should be handled properly.")
public class CommandAnnotationProcessor {

    private static final String REQUIRED_ARGUMENT_PLACEHOLDER = "$";

    /*
     yeah, this is a concurrent hash set.
     Be careful don't write the hashCode() for command descriptor, just use the memory address, use the command path to identify a command descriptor is possible to broken in some cases of register() and unregister().
     */
    public static final Set<CommandDescriptor> REGISTERED_COMMAND_DESCRIPTORS = ConcurrentHashMap.newKeySet();

    public static CommandDispatcher<ServerCommandSource> COMMAND_DISPATCHER;
    public static CommandRegistryAccess COMMAND_REGISTRY_ACCESS;

    public static void process() {
        /*
         note that: the `/reload` will trigger the `REGISTRATION` event.
         Also, the vanilla minecraft will remove all commands, so we must register fuji commands after reload.
         */
        CommandEvents.REGISTRATION.register((dispatcher, registryAccess, environment) -> {
            /* environment */
            CommandAnnotationProcessor.COMMAND_DISPATCHER = dispatcher;
            CommandAnnotationProcessor.COMMAND_REGISTRY_ACCESS = registryAccess;

            /* register argument type adapters */
            BaseArgumentTypeAdapter.Registry.registerTypeAdapters();

            /* register commands */
            REGISTERED_COMMAND_DESCRIPTORS.clear();
            processClasses();
        });
    }

    private static void processClasses() {
        ModuleManager.MODULE_INITIALIZER_BY_CLASS.values()
            .stream()
            .filter(Objects::nonNull)
            .forEach(initializer -> processClass(initializer.getClass()));
    }

    private static void processClass(Class<?> clazz) {
        ReflectionUtil
            .getMethodsWithAnnotation(clazz, CommandNode.class)
            .forEach(method -> processMethod(clazz, method));
    }

    private static void processMethod(Class<?> clazz, Method method) {
        /* verify */
        if (!method.getReturnType().equals(int.class)) {
            throw new RuntimeException("The method `%s` in class `%s` must return the primitive int data type.".formatted(method.getName(), clazz.getName()));
        }

        if (!Modifier.isStatic(method.getModifiers())) {
            throw new RuntimeException("The method `%s` in class `%s` must be static.".formatted(method.getName(), clazz.getName()));
        }

        /* make command descriptor */
        CommandDescriptor descriptor = makeCommandDescriptor(clazz, method);
        descriptor.register();

        /* make retarget command descriptor */
        RetargetCommandDescriptor
            .make(descriptor)
            .ifPresent(CommandDescriptor::register);
    }

    private static Class<?> unbox(Parameter parameter) {
        if (parameter.getType().equals(Optional.class)) {
            ParameterizedType parameterizedType = (ParameterizedType) parameter.getParameterizedType();
            return (Class<?>) parameterizedType.getActualTypeArguments()[0];
        }

        return parameter.getType();
    }

    private static boolean isRequiredArgumentPlaceholder(CommandArgument commandArgument) {
        return commandArgument.getArgumentName().startsWith(REQUIRED_ARGUMENT_PLACEHOLDER);
    }

    private static int parseMethodParameterIndexFromArgumentName(CommandArgument commandArgument) {
        // parse the method parameter index
        String argumentName = commandArgument.getArgumentName();
        if (argumentName.startsWith(REQUIRED_ARGUMENT_PLACEHOLDER)) {
            return Integer.parseInt(argumentName.substring(REQUIRED_ARGUMENT_PLACEHOLDER.length()));
        }

        throw new IllegalArgumentException("failed to parse parameter index from argument name for argument" + commandArgument);
    }

    private static @NotNull CommandDescriptor makeCommandDescriptor(Class<?> clazz, Method method) {
        List<CommandArgument> commandArgumentList = new ArrayList<>();

        /* process the @CommandNode above the class. */
        CommandNode classAnnotation = clazz.getAnnotation(CommandNode.class);
        CommandRequirement classRequirement = clazz.getAnnotation(CommandRequirement.class);
        if (classAnnotation != null && !classAnnotation.value().isBlank()) {
            Arrays.stream(classAnnotation.value().trim().split(" "))
                .filter(it -> !it.isBlank())
                .forEach(argumentName -> commandArgumentList.add(
                    CommandArgument.ofLiteralArgument(argumentName, CommandRequirementDescriptor.of(classRequirement))
                ));
        }

        /* process the @CommandNode above the method. */
        method.setAccessible(true);
        CommandNode methodAnnotation = method.getAnnotation(CommandNode.class);

        if (methodAnnotation.topLevel()) {
            commandArgumentList.clear();
        }

        CommandRequirement methodRequirement = null;
        for (String argumentName : Arrays.stream(methodAnnotation.value().trim().split(" "))
            .filter(node -> !node.isBlank())
            .toList()) {

            /* pass the class requirement down, if the method requirement is null */
            methodRequirement = method.getAnnotation(CommandRequirement.class);
            if (methodRequirement == null) {
                methodRequirement = classRequirement;
            }

            /* make requirement descriptor */
            commandArgumentList.add(CommandArgument.ofLiteralArgument(argumentName, CommandRequirementDescriptor.of(methodRequirement)));
        }

        /* process the required arguments */
        boolean hasAnyRequiredArgumentPlaceholder = commandArgumentList.stream().anyMatch(CommandAnnotationProcessor::isRequiredArgumentPlaceholder);
        if (hasAnyRequiredArgumentPlaceholder) {
            /* specify the mappings between argument and parameter manually.  */
            for (int argumentIndex = 0; argumentIndex < commandArgumentList.size(); argumentIndex++) {
                /* find $1, $2 ... and replace them with the correct argument. */
                CommandArgument commandArgument = commandArgumentList.get(argumentIndex);
                if (!isRequiredArgumentPlaceholder(commandArgument)) continue;

                /* replace the required argument placeholder `$1` with the parameter in method whose index is 1*/
                int methodParameterIndex = parseMethodParameterIndexFromArgumentName(commandArgument);
                Parameter parameter = method.getParameters()[methodParameterIndex];
                Class<?> type = unbox(parameter);
                boolean isOptional = parameter.getType().equals(Optional.class);
                commandArgumentList.set(argumentIndex,
                    CommandArgument
                        .ofRequiredArgument(type, parameter.getName(), isOptional, CommandRequirementDescriptor.of(methodRequirement))
                        .fillParameter(parameter)
                );
            }
            /* generate the command source argument for lazy programmers. */
            for (int parameterIndex = 0; parameterIndex < method.getParameters().length; parameterIndex++) {
                Parameter parameter = method.getParameters()[parameterIndex];
                if (parameter.getAnnotation(CommandSource.class) == null) continue;
                Class<?> type = unbox(parameter);
                // for a command source argument, we don't care the index
                commandArgumentList.add(0, CommandArgument
                    .ofRequiredArgument(type, parameter.getName(), false, CommandRequirementDescriptor.of(methodRequirement))
                    .fillParameter(parameter)
                );
            }
        } else {
            /* generate the mappings between argument and parameter automatically. */
            Parameter[] parameters = method.getParameters();
            for (Parameter parameter : parameters) {
                /* append the argument to the tail*/
                Class<?> type = unbox(parameter);
                boolean isOptional = parameter.getType().equals(Optional.class);
                CommandArgument commandArgument = CommandArgument
                    .ofRequiredArgument(type, parameter.getName(), isOptional, CommandRequirementDescriptor.of(methodRequirement))
                    .fillParameter(parameter);
                commandArgumentList.add(commandArgument);
            }
        }

        /* verify */
        if (commandArgumentList.isEmpty()) {
            throw new RuntimeException("The argument list of @CommandNode annotated in method `%s` in class `%s` is empty.".formatted(method.getName(), clazz.getName()));
        }

        return new CommandDescriptor(method, commandArgumentList)
            .fillDocument(method.getAnnotation(Document.class));
    }

}
