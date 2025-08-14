package io.github.sakurawald.fuji.core.command.descriptor;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import io.github.sakurawald.fuji.core.auxiliary.LogUtil;
import io.github.sakurawald.fuji.core.auxiliary.ReflectionUtil;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.LuckpermsHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.fuji.core.command.argument.adapter.abst.BaseArgumentTypeAdapter;
import io.github.sakurawald.fuji.core.command.argument.structure.CommandArgument;
import io.github.sakurawald.fuji.core.command.exception.AbortCommandExecutionException;
import io.github.sakurawald.fuji.core.command.extension.CommandNodeExtension;
import io.github.sakurawald.fuji.core.command.processor.CommandAnnotationProcessor;
import io.github.sakurawald.fuji.core.command.structure.CommandRequirementDescriptor;
import io.github.sakurawald.fuji.core.config.Configs;
import io.github.sakurawald.fuji.core.document.annotation.DocStringProvider;
import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.core.document.annotation.ForDeveloper;
import io.github.sakurawald.fuji.core.document.annotation.TestCase;
import io.github.sakurawald.fuji.core.document.descriptor.PermissionDescriptor;
import io.github.sakurawald.fuji.core.document.interfaces.SourceModuleGetter;
import io.github.sakurawald.fuji.core.manager.impl.module.ModuleManager;
import io.github.sakurawald.fuji.core.structure.Pair;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@RequiredArgsConstructor
@ForDeveloper("""
    A command descriptor is used to describe a command instance.
    """)
public class CommandDescriptor implements SourceModuleGetter {

    public final @NotNull Method method;

    public final @NotNull List<CommandArgument> commandArguments;

    public @Nullable String document;

    private @Nullable LiteralArgumentBuilder<ServerCommandSource> registerReturnValue;

    public @NotNull CommandDescriptor fillDocument(@Nullable Document document) {
        if (document == null) return this;
        return this.fillDocument(document.value());
    }

    public @NotNull CommandDescriptor fillDocument(@Nullable String document) {
        if (document == null) return this;
        this.document = document;
        return this;
    }

    public void register() {
        LogUtil.debug("Register command: {}", this);

        /* First pass: build the non-optional arguments. */
        registerNonOptionalArguments();

        /* Second pass: build the optional arguments. */
        registerOptionalArguments();

        /* Sync the registry. */
        CommandAnnotationProcessor.REGISTERED_COMMAND_DESCRIPTORS.add(this);
    }

    public void unregister() {
        LogUtil.debug("Un-register command: {}", this);

        RootCommandNode<ServerCommandSource> root = CommandAnnotationProcessor.COMMAND_DISPATCHER.getRoot();
        assert this.registerReturnValue != null;
        LiteralCommandNode<ServerCommandSource> navigationNode = this.registerReturnValue.build();
        CommandNode<ServerCommandSource> startNode = root.getChild(navigationNode.getName());
        if (startNode != null) {
            if (CommandDescriptor.unregisterRecursively(startNode, navigationNode)) {
                root.getChildren()
                    .removeIf(commandNode -> commandNode.getName().equals(navigationNode.getName()));
            }
        }

        /* Sync the registry. */
        CommandAnnotationProcessor.REGISTERED_COMMAND_DESCRIPTORS.remove(this);
    }

    private static boolean unregisterRecursively(@Nullable CommandNode<ServerCommandSource> targetNode, @NotNull CommandNode<ServerCommandSource> navigationNode) {
        /* If there is no target node in the server command tree, return true to report empty. */
        if (targetNode == null) {
            return true;
        }

        /* Go down with the navigation node. */
        navigationNode.getChildren()
            .stream()
            .toList()
            .forEach(child -> {
                if (unregisterRecursively(targetNode.getChild(child.getName()), child)) {
                    // NODE: Identify the `command node` by its name, should not use `equals` method.
                    targetNode
                        .getChildren()
                        .removeIf(it -> it.getName().equals(child.getName()));
                }
            });

        /* Return if target node is empty. */
        return targetNode.getChildren() == null
            || targetNode.getChildren().isEmpty();
    }

    @Override
    public final boolean equals(Object obj) {
        return this == obj;
    }

    @Override
    public final int hashCode() {
        return super.hashCode();
    }

    @ForDeveloper("Returns the only possible path to the command node.")
    public @NotNull String getCommandNodePath() {
        assert this.registerReturnValue != null;
        return getCommandNodePathRecursively(this.registerReturnValue.build());
    }

    private static @NotNull String getCommandNodePathRecursively(@NotNull CommandNode<ServerCommandSource> registeredRootNode) {
        StringBuilder commandPath = new StringBuilder();
        commandPath.append(registeredRootNode.getName());

        registeredRootNode
            .getChildren()
            .forEach(child -> commandPath
                .append(".")
                .append(getCommandNodePathRecursively(child)));
        return commandPath.toString();
    }

    private static @NotNull CommandNode<ServerCommandSource> findOptionalArgumentAnchor(@NotNull List<CommandArgument> commandArguments) {
        List<String> commandPath = commandArguments
            .stream()
            .filter(arg -> !arg.isCommandSource())
            .takeWhile(arg -> !arg.isOptional())
            .map(CommandArgument::getArgumentName)
            .toList();

        return CommandAnnotationProcessor.COMMAND_DISPATCHER.findNode(commandPath);
    }

    protected @NotNull List<CommandArgument> getMethodParameterSpecifiers() {
        return this.commandArguments
            .stream()
            /* Filter out the literal command node and root command node. */
            .filter(CommandArgument::isMethodParameterSpecifier)
            .toList();
    }

    protected @NotNull List<CommandArgument> getCommandArguments() {
        return this.commandArguments;
    }

    protected @NotNull List<Object> makeMethodParameterValues(@NotNull CommandContext<ServerCommandSource> ctx) {
        List<Object> args = new ArrayList<>();

        for (CommandArgument commandArgument : this.getMethodParameterSpecifiers()) {
            /* inject the value into a required argument. */
            try {
                Object arg = BaseArgumentTypeAdapter.Registry
                    .getTypeAdapter(commandArgument.getArgumentType())
                    .makeParameterValue(ctx, commandArgument);

                args.add(arg);
            } catch (Exception e) {
                /*
                 * for command redirect, given 3 optional arguments named x, y and z.
                 * The arguments are defined in order: (x, y, z).
                 * The optional argument must be passed in the order that matches the defined order.
                 * If the command source pass the optional arguments in the order (z, x, y), then thw following exceptions will be thrown:
                 * java.lang.IllegalArgumentException, e.message = No such argument 'x' exists on this command
                 * java.lang.IllegalArgumentException, e.message = No such argument 'y' exists on this command
                 *
                 * In order to continue the command-context passing process, we will temporally ignore the exception, so that the optional argument can be filled properly.
                 *
                 * The magic field "No such argument" is thrown by mojang's brigadier system.
                 * */
                if (e.getMessage() != null && e.getMessage().startsWith("No such argument")) {
                    args.add(Optional.empty());
                    continue;
                }

                // Throw other exceptions to upper-level handler.
                throw e;
            }

        }

        return args;
    }

    protected Optional<Integer> findCommandSourceMethodParameterSpecifierIndex() {
        return findMethodParameterSpecifierIndex(CommandArgument::isCommandSource);
    }

    protected Optional<Integer> findCommandTargetMethodParameterSpecifierIndex() {
        return findMethodParameterSpecifierIndex(CommandArgument::isCommandTarget);
    }

    protected Optional<Integer> findMethodParameterSpecifierIndex(@NotNull Predicate<CommandArgument> predicate) {
        List<CommandArgument> parameterSpecifiers = this.getMethodParameterSpecifiers();
        for (int i = 0; i < parameterSpecifiers.size(); i++) {
            CommandArgument commandArgument = parameterSpecifiers.get(i);
            if (predicate.test(commandArgument)) {
                return Optional.of(i);
            }
        }

        return Optional.empty();
    }

    protected Optional<Integer> findCommandArgumentIndex(@NotNull Predicate<CommandArgument> predicate) {
        List<CommandArgument> commandArguments = this.getCommandArguments();
        for (int i = 0; i < commandArguments.size(); i++) {
            CommandArgument commandArgument = commandArguments.get(i);
            if (predicate.test(commandArgument)) {
                return Optional.of(i);
            }
        }

        return Optional.empty();
    }

    protected @NotNull Command<ServerCommandSource> makeCommandAction() {
        return (commandContext) -> {
            int commandReturnValue;
            try {
                /* Verify the command source. */
                if (!CommandSource.verifyCommandSource(commandContext, this)) {
                    return CommandHelper.Return.FAILURE;
                }

                /* invoke the command function */
                List<Object> parameterValues = makeMethodParameterValues(commandContext);
                commandReturnValue = (int) this.method.invoke(null, parameterValues.toArray());
            } catch (Exception wrappedOrUnwrappedException) {
                return CommandException.handleCommandException(commandContext, this.method, wrappedOrUnwrappedException);
            }

            return commandReturnValue;
        };
    }

    private void registerNonOptionalArguments() {
        /* Make the assembled argument builder. */
        List<ArgumentBuilder<ServerCommandSource, ?>> argumentBuilders = ArgumentBuilderMaker.makeNonOptionalArgumentBuilders(this);
        Command<ServerCommandSource> commandAction = makeCommandAction();
        LiteralArgumentBuilder<ServerCommandSource> assembledArgumentBuilder = ArgumentBuilderMaker.assembleArgumentBuilders(argumentBuilders, commandAction);

        /* Register the assembled argument builder as the child of the global root argument builder. */
        CommandAnnotationProcessor.COMMAND_DISPATCHER.register(assembledArgumentBuilder);
        this.registerReturnValue = assembledArgumentBuilder;
    }

    @ForDeveloper("""
        Register all `optional arguments` and redirect them into the `anchor command node`.
        This method should not handle the command requirements, since it's already done while registering the non-optional arguments.
        """)
    private void registerOptionalArguments() {
        CommandNode<ServerCommandSource> redirectTargetNode = findOptionalArgumentAnchor(this.commandArguments);
        this.commandArguments
            .stream()
            .filter(CommandArgument::isOptional)
            .forEach(optionalArgument -> {
                /* Make the builder for the optional argument. */
                ArgumentBuilder<ServerCommandSource, ?> optionalArgumentBuilder =
                    CommandManager
                        .literal("--" + optionalArgument.getArgumentName())
                        .then(ArgumentBuilderMaker
                            .makeRequiredArgumentBuilder(optionalArgument)
                            .executes(redirectTargetNode.getCommand())
                            .redirect(redirectTargetNode));

                /* Register the optional argument builder as the child of the redirect target node. */
                redirectTargetNode.addChild(optionalArgumentBuilder.build());
            });
    }

    public static class CommandRequirement {

        private static int computeLevelPermission(@NotNull CommandDescriptor descriptor) {
            int minRequiredLevel = CommandRequirementDescriptor.getInitialLevel();

            for (CommandArgument commandArgument : descriptor.commandArguments) {
                if (commandArgument.getRequirement() == null) {
                    continue;
                }
                int permissionLevel = commandArgument.getRequirement().getLevel();
                minRequiredLevel = Math.max(minRequiredLevel, permissionLevel);
            }
            return minRequiredLevel;
        }

        private static @Nullable String computeStringPermission(@NotNull CommandDescriptor descriptor) {
            @Nullable String requiredString = CommandRequirementDescriptor.getInitialString();
            for (CommandArgument commandArgument : descriptor.commandArguments) {
                if (commandArgument.getRequirement() == null) {
                    continue;
                }

                String permissionString = commandArgument.getRequirement().getString();
                if (permissionString != null && !permissionString.isBlank()) {
                    requiredString = permissionString;
                    break;
                }
            }

            return requiredString;
        }

        public static @NotNull CommandRequirementDescriptor computeCommandRequirement(@NotNull CommandDescriptor descriptor) {
            int levelPermission = computeLevelPermission(descriptor);
            String stringPermission = computeStringPermission(descriptor);
            return new CommandRequirementDescriptor(levelPermission, stringPermission);
        }

        @TestCase(action = "Issue the `/warp` and `/back` command as normal user.", targets = {
            "The default command permission should be registered properly."
            , "A public command, that shares a common command path prefix with another admin command, should be accessible to normal users."
        })
        @DocStringProvider(id = 1751999362278L, value = "The permission used as the default string permission, for a command descriptor.")
        private static void fillCommandRequirement(@NotNull List<Pair<ArgumentBuilder<ServerCommandSource, ?>, CommandArgument>> pairs, @NotNull CommandDescriptor descriptor) {
            /* Fill the command requirements based on the command arguments. */
            String walkingCommandPath = "";
            boolean seenAnyNonNullRequiremnt = false;
            for (var pair : pairs) {
                /* Extract the key and value. */
                ArgumentBuilder<ServerCommandSource, ?> argumentBuilder = pair.getKey();
                CommandArgument commandArgument = pair.getValue();

                /* Update the walking path. */
                walkingCommandPath = walkingCommandPath + "." + commandArgument.getArgumentName();
                walkingCommandPath = CommandHelper.Node.trimCommandPathString(walkingCommandPath);

                /* Track the public command prefix path. */
                if (!seenAnyNonNullRequiremnt && CommandRequirementDescriptor.isEmptyRequirement(commandArgument.getRequirement())) {
                    if (!CommandAnnotationProcessor.PUBLIC_COMMAND_PATHS.contains(walkingCommandPath)) {
                        LogUtil.debug("Add command path '{}' as the path of public command.", walkingCommandPath);
                        CommandAnnotationProcessor.PUBLIC_COMMAND_PATHS.add(walkingCommandPath);

                        // NOTE: Update the existing command nodes in the path, if they are registered before by some non-public commands.
                        CommandHelper.Node
                            .findCommandNode(walkingCommandPath)
                            .ifPresent(registeredCommandNode -> {
                                @SuppressWarnings("unchecked")
                                CommandNodeExtension<ServerCommandSource> extension = ((CommandNodeExtension<ServerCommandSource>) registeredCommandNode);
                                extension.fuji$setRequirement((source) -> true);
                            });
                    }

                    // For a public command prefix path, skip setting the requirements.
                    continue;
                }

                /* Stop tracking the public command prefix path, since we have seen a specified requirement. */
                seenAnyNonNullRequiremnt = true;

                if (CommandAnnotationProcessor.PUBLIC_COMMAND_PATHS.contains(walkingCommandPath)) {
                    LogUtil.debug("Skip setting the requirement for the path of public command: {}", walkingCommandPath);
                } else {
                    /* Resolve the command requirement from the command descriptor. */
                    CommandRequirementDescriptor requirement = computeCommandRequirement(descriptor);

                    /* Make the command requirement predicate. */
                    Predicate<ServerCommandSource> predicate = makeCommandRequirementPredicate(requirement);

                    /* Set this predicate. */
                    argumentBuilder.requires(predicate);
                }
            }

        }

        @SuppressWarnings("RedundantIfStatement")
        private static @NotNull Predicate<ServerCommandSource> makeCommandRequirementPredicate(CommandRequirementDescriptor requirement) {
            return (commandContext) -> {
                ServerPlayerEntity player = commandContext.getPlayer();

                /* The console can use all commands. */
                if (player == null) return true;

                /* Check the string permission. */
                if (requirement.getString() != null
                    && !requirement.getString().isEmpty()
                    && LuckpermsHelper.hasPermission(player.getUuid(), new PermissionDescriptor(requirement.getString(), 1751999362278L))) {
                    return true;
                }

                /* Check the level permission. */
                if (commandContext.hasPermissionLevel(requirement.getLevel())) {
                    return true;
                }

                /* Insufficient permission to use this command. */
                return false;
            };
        }

        public static void setEffectiveDefaultCommandRequirement(@NotNull CommandDescriptor descriptor) {
            /* Compute the effective default command requirement for the full command path. */
            String fullCommandPath = descriptor.commandArguments
                .stream()
                .filter(CommandArgument::isCommandArgumentSpecifier)
                .map(CommandArgument::getArgumentName)
                .collect(Collectors.joining("."));

            CommandRequirementDescriptor defaultCommandRequirement = computeCommandRequirement(descriptor);

            Map<String, Integer> permissionMap = CommandAnnotationProcessor.permission.model()
                .getDefaultLevelPermission()
                .getCommands();
            int effectiveDefaultLevelPermission = permissionMap.computeIfAbsent(fullCommandPath, k -> defaultCommandRequirement.getLevel());

            /* Make the effective command requirement. */
            CommandRequirementDescriptor effectiveDefaultCommandRequirement;
            if (Configs.MAIN_CONTROL_CONFIG.model().core.permission.all_commands_require_level_4_permission_to_use_by_default) {
                effectiveDefaultCommandRequirement = new CommandRequirementDescriptor(4, null);
            } else {
                effectiveDefaultCommandRequirement = new CommandRequirementDescriptor(effectiveDefaultLevelPermission, null);
            }

            /* Apply the requirement for the command arguments. */
            descriptor.commandArguments
                .forEach(it -> it.setRequirement(effectiveDefaultCommandRequirement));
        }
    }

    public static class CommandException {

        @SuppressWarnings("SameReturnValue")
        public static int handleCommandException(CommandContext<ServerCommandSource> ctx, Method method, Exception wrappedOrUnwrappedException) {
            /* get the real exception during reflection. */
            Throwable theRealException = wrappedOrUnwrappedException;
            if (wrappedOrUnwrappedException instanceof InvocationTargetException) {
                theRealException = wrappedOrUnwrappedException.getCause();
            }

            /* handle AbortCommandExecutionException */
            if (theRealException instanceof AbortCommandExecutionException) {
                // the logging is done before throwing the AbortOperationException, here we just swallow this exception.
                return CommandHelper.Return.FAILURE;
            }

            /* report the exception */
            reportException(ctx.getSource(), method, theRealException);
            return CommandHelper.Return.FAILURE;
        }

        protected static void reportException(ServerCommandSource source, Method method, Throwable throwable) {
            /* report to console */
            String errorString = """
                [Fuji Exception Catcher]
                - Source: %s
                - Module: %s
                - Method: %s
                - Message: %s

                """.formatted(
                source.getName()
                , ModuleManager.computeSplitModulePath(method.getDeclaringClass().getName())
                , method.getName()
                , throwable);
            LogUtil.error(errorString, throwable);

            /* report to command source */
            Style style = Style.EMPTY
                .withColor(CommandHelper.COMMAND_EXCEPTION_COLOR_INT);

            // NOTE: Only send the stack trace if the command source is admin.
            if (CommandHelper.Requirement.isAdmin(source)) {
                String stacktrace = String.join("\n", ReflectionUtil.extractStackTraceElements(throwable));
                style
                    .withHoverEvent(TextHelper.Events.HoverEvent.makeShowTextAction(Text.of("Click to copy the stacktrace.")))
                    .withClickEvent(TextHelper.Events.ClickEvent.makeCopyToClipboardAction(stacktrace));
            }

            MutableText report = TextHelper.getTextByValue(source, errorString)
                .copy()
                .setStyle(style);

            source.sendMessage(report);
        }
    }

    private static class ArgumentBuilderMaker {

        private static @NotNull LiteralArgumentBuilder<ServerCommandSource> makeLiteralArgumentBuilder(@NotNull CommandArgument commandArgument) {
            return CommandManager.literal(commandArgument.getArgumentName());
        }

        private static @NotNull RequiredArgumentBuilder<ServerCommandSource, ?> makeRequiredArgumentBuilder(@NotNull CommandArgument commandArgument) {
            /* use adapter to make the required argument builder */
            return BaseArgumentTypeAdapter.Registry
                .getTypeAdapter(commandArgument.getArgumentType())
                .makeRequiredArgumentBuilder(commandArgument.getArgumentName());
        }

        @SuppressWarnings("unchecked")
        private static @NotNull LiteralArgumentBuilder<ServerCommandSource> assembleArgumentBuilders(@NotNull List<ArgumentBuilder<ServerCommandSource, ?>> builders, @NotNull Command<ServerCommandSource> commandAction) {
            /* Assemble the argument builders into one argument builder. */
            ArgumentBuilder<ServerCommandSource, ?> root = null;

            for (int i = builders.size() - 1; i >= 0; i--) {
                ArgumentBuilder<ServerCommandSource, ?> node = builders.get(i);
                if (root == null) {
                    root = node;
                    root = root.executes(commandAction);
                    continue;
                }
                root = node.then(root);
            }

            /* Return the assembled argument builder. */
            if (!(root instanceof LiteralArgumentBuilder)) {
                throw new IllegalArgumentException("The first argument builder must be a literal argument builder.");
            }
            return (LiteralArgumentBuilder<ServerCommandSource>) root;
        }

        private static @NotNull ArgumentBuilder<ServerCommandSource, ?> makeArgumentBuilder(@NotNull CommandArgument commandArgument) {
            ArgumentBuilder<ServerCommandSource, ?> builder;
            if (commandArgument.isRequiredArgument()) {
                builder = makeRequiredArgumentBuilder(commandArgument);
            } else {
                builder = makeLiteralArgumentBuilder(commandArgument);
            }
            return builder;
        }

        private static List<ArgumentBuilder<ServerCommandSource, ?>> makeNonOptionalArgumentBuilders(@NotNull CommandDescriptor descriptor) {
            List<Pair<ArgumentBuilder<ServerCommandSource, ?>, CommandArgument>> pairs = new ArrayList<>();
            descriptor.commandArguments
                .stream()
                .filter(
                    it ->
                        // Ignore the optional arguments, since we will process them in the second pass.
                        !it.isOptional()
                            && it.isCommandArgumentSpecifier())
                .forEach(argument -> {
                    /* Make the argument builder. */
                    ArgumentBuilder<ServerCommandSource, ?> builder = makeArgumentBuilder(argument);

                    /* Add the builder. */
                    pairs.add(new Pair<>(builder, argument));
                });

            /* Fill the command requirement for the builders. */
            CommandRequirement.fillCommandRequirement(pairs, descriptor);

            /* Return the builders. */
            return pairs
                .stream()
                .map(Pair::getKey)
                .collect(Collectors.toList());
        }

    }

    public static class CommandSource {

        @SuppressWarnings("BooleanMethodIsAlwaysInverted")
        protected static boolean verifyCommandSource(@NotNull CommandContext<ServerCommandSource> commandContext, @NotNull CommandDescriptor descriptor) {
            List<CommandArgument> expectedCommandSources = descriptor.commandArguments
                .stream()
                .filter(CommandArgument::isCommandSource)
                .toList();

            // Yeah, any type of source can use it.
            if (expectedCommandSources.isEmpty()) return true;
            // Oh no, specify too many command sources.
            if (expectedCommandSources.size() > 1)
                throw new IllegalArgumentException("Expected only one argument as the command source: " + descriptor);

            // Verify the expected command source.
            return BaseArgumentTypeAdapter.Registry
                .getTypeAdapter(expectedCommandSources.get(0).getArgumentType())
                .verifyCommandSource(commandContext);
        }

    }

    public boolean canBeExecutedByConsole() {
        return this.commandArguments
            .stream()
            .filter(CommandArgument::isCommandSource)
            .allMatch(commandArgument ->
                commandArgument.getArgumentType().equals(CommandContext.class)
                    || commandArgument.getArgumentType().equals(ServerCommandSource.class));
    }

    @Override
    public String toString() {
        return "/" + this.commandArguments
            .stream()
            .map(CommandArgument::toString)
            .collect(Collectors.joining(" "));
    }

    public String getCommandSyntax() {
        return "/" + this.commandArguments
            .stream()
            .filter(CommandArgument::isCommandArgumentSpecifier)
            .map(CommandArgument::toFriendlyString)
            .collect(Collectors.joining(" "));
    }

    @Override
    public String getSourceModule() {
        return ModuleManager.computeJoinedModulePath(this.method.getDeclaringClass().getName());
    }

}
