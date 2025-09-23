package mod.fuji.core.command.descriptor;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import java.util.Collection;
import mod.fuji.core.auxiliary.LogUtil;
import mod.fuji.core.auxiliary.ReflectionUtil;
import mod.fuji.core.auxiliary.minecraft.CommandHelper;
import mod.fuji.core.auxiliary.minecraft.LuckpermsHelper;
import mod.fuji.core.auxiliary.minecraft.TextHelper;
import mod.fuji.core.command.argument.adapter.abst.BaseArgumentTypeAdapter;
import mod.fuji.core.command.argument.structure.CommandArgument;
import mod.fuji.core.command.exception.AbortCommandExecutionException;
import mod.fuji.core.command.extension.CommandNodeExtension;
import mod.fuji.core.command.processor.CommandAnnotationProcessor;
import mod.fuji.core.command.structure.CommandRequirementDescriptor;
import mod.fuji.core.command.structure.RegisteredCommandNode;
import mod.fuji.core.config.Configs;
import mod.fuji.core.document.annotation.DocStringProvider;
import mod.fuji.core.document.annotation.Document;
import mod.fuji.core.document.annotation.ForDeveloper;
import mod.fuji.core.document.annotation.TestCase;
import mod.fuji.core.document.descriptor.PermissionDescriptor;
import mod.fuji.core.document.interfaces.SourceModuleGetter;
import mod.fuji.core.manager.impl.module.ModulePathResolver;
import mod.fuji.core.structure.Pair;
import mod.fuji.core.structure.SpecialVariable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import lombok.SneakyThrows;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@ForDeveloper("""
    A command descriptor is used to describe a command instance.

    To be simple: A command descriptor = command action method + command argument list
    """)
public class CommandDescriptor implements SourceModuleGetter, ConsoleSpammer {

    public static final SpecialVariable<Boolean> stdoutSpecialVariable = new SpecialVariable<>(false);
    public static final SpecialVariable<Boolean> silentSpecialVariable = new SpecialVariable<>(false);
    private static final String SILENT_LITERAL = "silent";
    private static final String STDOUT_LITERAL = "stdout";

    public final @NotNull Method method;

    public final @NotNull List<CommandArgument> commandArguments;

    public Optional<String> document = Optional.empty();

    private Optional<LiteralArgumentBuilder<ServerCommandSource>> registerReturnValue = Optional.empty();

    protected CommandDescriptor(@NotNull Method method, @NotNull List<CommandArgument> commandArguments) {
        this.method = method;
        this.commandArguments = commandArguments;
    }

    @CanIgnoreReturnValue
    public @NotNull CommandDescriptor fillDocument(@NotNull Optional<String> document) {
        if (document.isEmpty()) return this;
        this.document = document;
        return this;
    }

    @CanIgnoreReturnValue
    public @NotNull CommandDescriptor fillDocument(@Nullable Document document) {
        if (document == null) return this;
        return this.fillDocument(document.value());
    }

    @CanIgnoreReturnValue
    public @NotNull CommandDescriptor fillDocument(@Nullable String document) {
        if (document == null) return this;
        this.document = Optional.of(document);
        return this;
    }

    public void register() {
        trySpamConsole(() -> LogUtil.info("Register {} command: {}", this.getClass().getSimpleName(), this.getUserFriendlyCommandSyntax()));
        LogUtil.debug("Register command: {}", this);

        /* First pass: build the non-optional arguments. */
        registerNonOptionalArguments();

        /* Second pass: build the optional arguments. */
        registerOptionalArguments();

        /* Sync the registry. */
        CommandAnnotationProcessor.REGISTERED_COMMAND_DESCRIPTORS.add(this);
    }

    @TestCase(action = "Modify the `my-command` into `my-command-v2`, and issue `/fuji reload`.", targets = {
        "The command descriptor should be able to un-register the old command node in the command tree, even the new command node has different structure compared to the old one."
    })
    public void unregister() {
        trySpamConsole(() -> LogUtil.info("Un-Register {} command: {}", this.getClass().getSimpleName(), this.getUserFriendlyCommandSyntax()));
        LogUtil.debug("Un-register command: {}", this);

        this.registerReturnValue
            .ifPresentOrElse($registerReturnValue -> {
                /* Find the registered command tree. */
                LiteralCommandNode<ServerCommandSource> navigationNode = $registerReturnValue.build();
                List<List<RegisteredCommandNode>> registeredCommandTree = CommandTree.findRegisteredCommandTree(navigationNode);
                if (registeredCommandTree.isEmpty()) {
                    LogUtil.warn("The command '{}' not found in server command tree, ignoring its un-registration.", this.getUserFriendlyCommandSyntax());
                    return;
                }

                /* Cut-down the registered command tree. */
                LogUtil.debug("Un-register the command tree: {}", registeredCommandTree);
                registeredCommandTree
                    .forEach(branch -> branch.forEach(CommandTree::removeSelfInCommandTree));

            }, () -> LogUtil.warn("Failed to remove the registered command node from the server command tree, due to the register return value being null. (descriptor = {}) ", this));

        /* Sync the registry. */
        CommandAnnotationProcessor.REGISTERED_COMMAND_DESCRIPTORS.remove(this);
    }

    @ForDeveloper("Test the equality using physical memory address.")
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
        return this.registerReturnValue
            .map($registerReturnValue -> getCommandNodePathRecursively($registerReturnValue.build()))
            .orElseGet(() -> {
                LogUtil.warn("Failed to get the command node path, due to the register return value being null currently. (descriptor = {})", this);
                return "[FAILED TO FIND PATH, COMMAND NODE UNREGISTERED]";
            });
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
            .filter(CommandArgument::isCommandArgumentSpecifier)
            .takeWhile(commandArgument -> !commandArgument.isOptional()
                // NOTE: Should not redirect to the greedy string argument type, or the greedy string will eat all the remaining input characters.
                && !commandArgument.isGreedyArgumentType())
            .map(CommandArgument::getArgumentName)
            .toList();

        return CommandHelper.getCommandDispatcher().findNode(commandPath);
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

    @TestCase(action = "Test the `optional argument` functionality.", targets = {
        "Issue `/send-title @s --mainTitle \"main\"`",
        "Issue `/send-title @s --mainTitle \"main\" --subTitle \"sub\"`",
        "Issue `/send-title @s --subTitle \"sub\" --mainTitle \"main\"`"
    })
    protected @NotNull List<Object> makeMethodParameterValues(@NotNull CommandContext<ServerCommandSource> ctx) {
        List<Object> parameterValues = new ArrayList<>();

        for (CommandArgument commandArgument : this.getMethodParameterSpecifiers()) {
            /* Inject the value into a required argument. */
            try {
                Object parameterValue = BaseArgumentTypeAdapter.Registry
                    .getTypeAdapter(commandArgument.getArgumentType())
                    .makeParameterValue(ctx, commandArgument);
                parameterValues.add(parameterValue);
            } catch (Exception e) {
                if (CommandException.isOptionalArgumentNotSpecifiedException(e)) {
                    if (commandArgument.isOptional()) {
                        parameterValues.add(Optional.empty());
                        continue;
                    } else {
                        LogUtil.error("""
                            [Lose argument values after command redirect]
                            The `argument values` are lost after a command redirect.
                            Related issue: https://github.com/Sinytra/Connector/issues/214

                            You should open an issue in https://github.com/fuji-fabric/fuji if you see this.
                            """);
                        throw new IllegalArgumentException("Lose argument values after command redirect.");
                    }
                }

                // Throw other exceptions to upper-level handler.
                throw e;
            }

        }

        return parameterValues;
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

    @SuppressWarnings({"UnnecessaryLocalVariable"})
    protected @NotNull Command<ServerCommandSource> makeCommandAction() {
        return withBaseCommandAction((commandContext) -> {
            try {
                /* Invoke the command function */
                List<Object> parameterValues = makeMethodParameterValues(commandContext);
                int commandReturnValue = (int) this.method.invoke(null, parameterValues.toArray());
                return commandReturnValue;
            } catch (Exception wrappedOrUnwrappedException) {
                return CommandException.handleCommandExecutionException(commandContext, this.method, wrappedOrUnwrappedException);
            }
        });
    }

    @SuppressWarnings("CodeBlock2Expr")
    protected final @NotNull Command<ServerCommandSource> withBaseCommandAction(@NotNull Function<CommandContext<ServerCommandSource>, Integer> commandAction) {
        return (commandContext) -> {
            /* Define the return value holder. */
            AtomicInteger commandReturnValue = new AtomicInteger();

            /* Define the special variables during the dynamic extent of this command execution. */
            Boolean stdoutFlag = CommandHelper.Context
                .tryGetArgument(commandContext, CommandDescriptor.STDOUT_LITERAL, Boolean.class)
                .orElse(stdoutSpecialVariable.get());

            Boolean silentFlag = CommandHelper.Context
                .tryGetArgument(commandContext, CommandDescriptor.SILENT_LITERAL, Boolean.class)
                .orElse(silentSpecialVariable.get());

            stdoutSpecialVariable.bind(stdoutFlag, () -> {
                silentSpecialVariable.bind(silentFlag, () -> {
                    /* Verify the command source. */
                    if (!CommandSource.verifyCommandSource(commandContext, this)) {
                        commandReturnValue.set(CommandHelper.Return.FAILURE);
                        return;
                    }

                    /* Call the wrapped command action. */
                    int apply = commandAction.apply(commandContext);
                    commandReturnValue.set(apply);
                });
            });

            /* Return the command return value to the top-level. */
            return commandReturnValue.get();
        };
    }

    private void registerNonOptionalArguments() {
        /* Make the assembled argument builder. */
        List<ArgumentBuilder<ServerCommandSource, ?>> argumentBuilders = ArgumentBuilderMaker.makeNonOptionalArgumentBuilders(this);
        LiteralArgumentBuilder<ServerCommandSource> assembledArgumentBuilder = ArgumentBuilderMaker.assembleArgumentBuilders(argumentBuilders, this::terminalArgumentDecorator);

        /* Register the assembled argument builder as the child of the global root argument builder. */
        CommandHelper.getCommandDispatcher().register(assembledArgumentBuilder);
        this.registerReturnValue = Optional.of(assembledArgumentBuilder);
    }

    protected @NotNull ArgumentBuilder<ServerCommandSource, ?> terminalArgumentDecorator(@NotNull ArgumentBuilder<ServerCommandSource, ?> terminalArgumentBuilder) {
        Command<ServerCommandSource> commandAction = makeCommandAction();
        return terminalArgumentBuilder.executes(commandAction);
    }

    @ForDeveloper("""
        Register all `optional arguments` and redirect them into the `anchor command node`.
        This method should not handle the command requirements, since it's already done while registering the non-optional arguments.
        """)
    private void registerOptionalArguments() {
        CommandNode<ServerCommandSource> redirectTargetNode = findOptionalArgumentAnchor(this.commandArguments);

        /* Register declared optional arguments. */
        this.commandArguments
            .stream()
            .filter(CommandArgument::isOptional)
            .forEach(optionalArgument -> registerOptionalArgument(optionalArgument, redirectTargetNode));

        /* Register global optional arguments. */
        // NOTE: The global optional arguments are registered into the server command tree directly, without modifying the command descriptor.
        CommandRequirementDescriptor requirement = new CommandRequirementDescriptor(4, null);
        registerOptionalArgument(CommandArgument.ofRequiredArgument(Boolean.class, SILENT_LITERAL, true, requirement), redirectTargetNode);
        registerOptionalArgument(CommandArgument.ofRequiredArgument(Boolean.class, STDOUT_LITERAL, true, requirement), redirectTargetNode);
    }

    private static void registerOptionalArgument(@NotNull CommandArgument optionalArgument, @NotNull CommandNode<ServerCommandSource> redirectTargetNode) {
        /* Make the leading literal argument for this optional argument. */
        CommandArgument leadingLiteralArgument = CommandArgument.ofLiteralArgument(getOptionalArgumentLeadingArgumentName(optionalArgument.getArgumentName()), optionalArgument.getRequirement());

        Predicate<ServerCommandSource> requirementPredicate = CommandRequirement.makeCommandRequirementPredicate(optionalArgument.getRequirement());

        /* Make the builder for the optional argument. */
        ArgumentBuilder<ServerCommandSource, ?> optionalArgumentBuilder =
            ArgumentBuilderMaker
                .makeLiteralArgumentBuilder(leadingLiteralArgument)
                .requires(requirementPredicate)
                .then(ArgumentBuilderMaker
                    .makeRequiredArgumentBuilder(optionalArgument)
                    .requires(requirementPredicate)
                    .executes(redirectTargetNode.getCommand())
                    .redirect(redirectTargetNode));

        /* Register the optional argument builder as the child of the redirect target node. */
        redirectTargetNode.addChild(optionalArgumentBuilder.build());
    }

    protected static @NotNull String getOptionalArgumentLeadingArgumentName(@NotNull String argumentName) {
        return "--" + argumentName;
    }

    @Override
    public boolean isConsoleSpammer() {
        return true;
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

            /* Remember this command path. */
            CommandAnnotationProcessor.LOADED_COMMAND_PATHS.add(fullCommandPath);

            /* Get the default command requirement from permission.json file. */
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
        @SneakyThrows(Throwable.class)
        public static int handleCommandExecutionException(@NotNull CommandContext<ServerCommandSource> context, @NotNull Method method, @NotNull Throwable throwable) {
            /* Unbox the real exception during reflection. */
            if (throwable instanceof InvocationTargetException) {
                throwable = throwable.getCause();
            }

            /* Handle AbortCommandExecutionException */
            if (throwable instanceof AbortCommandExecutionException) {
                // the logging is done before throwing the AbortOperationException, here we just swallow this exception.
                return CommandHelper.Return.FAILURE;
            }

            /* Re-throw the CommandSyntaxException to enclosing exception handler. */
            boolean isCommandSyntaxException = throwable instanceof CommandSyntaxException;
            if (isCommandSyntaxException) {
                throw throwable;
            } else {
                handleNonCommandSyntaxException(context, method, throwable);
            }

            return CommandHelper.Return.FAILURE;
        }

        private static void handleNonCommandSyntaxException(@NotNull CommandContext<ServerCommandSource> context, @NotNull Method method, @NotNull Throwable throwable) {
            ServerCommandSource source = context.getSource();

            /* Log error string to the console. */
            String nonCommandSyntaxErrorString = """
            [Command Execution Failed]
            - From Module: %s
            - Command String: /%s
            - Command Source: %s
            - Message: %s

            """.formatted(
                ModulePathResolver.computeModulePathString(method.getDeclaringClass().getName())
                , TextHelper.Parsers.escapeTags(context.getInput())
                , source.getName()
                , throwable);
            LogUtil.error(nonCommandSyntaxErrorString, throwable);

            /* Send error text to the command source. */
            Style errorTextStyle = Style.EMPTY
                .withColor(CommandHelper.COMMAND_EXCEPTION_COLOR_INT);

            // NOTE: Only send the stack trace if the command source is admin.
            if (CommandHelper.Requirement.isAdmin(source)) {
                String stacktrace = String.join("\n", ReflectionUtil.extractStackTraceElements(throwable));
                errorTextStyle = errorTextStyle
                    .withHoverEvent(TextHelper.Events.HoverEvent.makeShowTextAction(Text.of("Click to copy the stacktrace.")))
                    .withClickEvent(TextHelper.Events.ClickEvent.makeCopyToClipboardAction(stacktrace));
            }

            MutableText errorText = TextHelper.getTextByValue(source, nonCommandSyntaxErrorString)
                .copy()
                .setStyle(errorTextStyle);
            source.sendMessage(errorText);
        }

        private static boolean isOptionalArgumentNotSpecifiedException(@NotNull Exception e) {
            /*
             * For command redirect, given 3 optional arguments named x, y and z.
             * The arguments are defined in order: (x, y, z).
             * The optional argument must be passed in the order that matches the defined order.
             * If the command source pass the optional arguments in the order (z, x, y), then thw following exceptions will be thrown:
             * java.lang.IllegalArgumentException, e.message = No such argument 'x' exists on this command
             * java.lang.IllegalArgumentException, e.message = No such argument 'y' exists on this command
             *
             * In order to continue the command-context passing process, we will temporally ignore the exception, so that the optional argument can be filled properly.
             *
             * The magic field "No such argument" is thrown by mojang's brigadier system. (CommandContext#getArgument)
             * */
            return e.getMessage() != null && e.getMessage().startsWith("No such argument");
        }
    }

    private static class ArgumentBuilderMaker {

        private static @NotNull LiteralArgumentBuilder<ServerCommandSource> makeLiteralArgumentBuilder(@NotNull CommandArgument commandArgument) {
            if (!commandArgument.isLiteralArgument()) {
                throw new IllegalArgumentException("The command argument must be literal argument.");
            }

            return CommandManager
                .literal(commandArgument.getArgumentName());
        }

        private static @NotNull RequiredArgumentBuilder<ServerCommandSource, ?> makeRequiredArgumentBuilder(@NotNull CommandArgument commandArgument) {
            if (!commandArgument.isRequiredArgument()) {
                throw new IllegalArgumentException("The command argument must be required argument.");
            }

            /* use adapter to make the required argument builder */
            return BaseArgumentTypeAdapter.Registry
                .getTypeAdapter(commandArgument.getArgumentType())
                .makeComposedRequiredArgumentBuilder(commandArgument.getArgumentName());
        }

        @SuppressWarnings("unchecked")
        private static @NotNull LiteralArgumentBuilder<ServerCommandSource> assembleArgumentBuilders(@NotNull List<ArgumentBuilder<ServerCommandSource, ?>> builders, @NotNull Function<ArgumentBuilder<ServerCommandSource, ?>, ArgumentBuilder<ServerCommandSource, ?>> terminalArgumentDecorator) {
            /* Assemble the argument builders into one argument builder. */
            ArgumentBuilder<ServerCommandSource, ?> root = null;

            for (int i = builders.size() - 1; i >= 0; i--) {
                ArgumentBuilder<ServerCommandSource, ?> node = builders.get(i);
                if (root == null) {
                    root = node;
                    root = terminalArgumentDecorator.apply(root);
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

        @SuppressWarnings({"BooleanMethodIsAlwaysInverted", "SequencedCollectionMethodCanBeUsed"})
        protected static boolean verifyCommandSource(@NotNull CommandContext<ServerCommandSource> commandContext, @NotNull CommandDescriptor descriptor) {
            List<CommandArgument> expectedCommandSources = descriptor.commandArguments
                .stream()
                .filter(CommandArgument::isCommandSource)
                .toList();

            // Yeah, any type of source can use it.
            if (expectedCommandSources.isEmpty())
                return true;
            // Oh no, specify too many command sources.
            if (expectedCommandSources.size() > 1)
                throw new IllegalArgumentException("Expected ZERO or ONE argument as the command source: " + descriptor);

            // Verify the expected command source.
            return BaseArgumentTypeAdapter.Registry
                .getTypeAdapter(expectedCommandSources.get(0).getArgumentType())
                .verifyCommandSource(commandContext);
        }

    }

    public static class CommandTree {

        private static @NotNull List<List<RegisteredCommandNode>> findRegisteredCommandTree(@NotNull CommandNode<ServerCommandSource> navigationNode) {
            /* Set up the primary branch. */
            List<List<RegisteredCommandNode>> treeCollector = new ArrayList<>();
            List<RegisteredCommandNode> branchCollector = new ArrayList<>();
            treeCollector.add(branchCollector);

            /* Find recursively. */
            RootCommandNode<ServerCommandSource> root = CommandHelper.getCommandDispatcher().getRoot();
            findRegisteredCommandTreeRecursively(treeCollector, branchCollector, navigationNode, root);
            return treeCollector;
        }

        @ForDeveloper("Returns a chain of registered command nodes.")
        @SuppressWarnings("UnnecessaryLocalVariable")
        private static void findRegisteredCommandTreeRecursively(@NotNull List<List<RegisteredCommandNode>> treeCollector, @NotNull List<RegisteredCommandNode> branchCollector, @NotNull CommandNode<ServerCommandSource> navigationNode, @NotNull CommandNode<ServerCommandSource> walkingNode) {
            CommandNode<ServerCommandSource> parent = walkingNode;

            Optional
                .ofNullable(parent.getChild(navigationNode.getName()))
                .ifPresent(child -> {
                    /* Walk the path. */
                    RegisteredCommandNode found = new RegisteredCommandNode(parent, child);
                    branchCollector.add(found);

                    /* Go down. */
                    Collection<CommandNode<ServerCommandSource>> children = navigationNode.getChildren();
                    children
                        .forEach(newNavigationNode -> {
                            if (children.size() == 1) {
                                /* Only 1 branch, continuing it. */
                                findRegisteredCommandTreeRecursively(treeCollector, branchCollector, newNavigationNode, child);
                            } else {
                                /* More than 1 branch, forking it. */
                                ArrayList<RegisteredCommandNode> forkedCollector = new ArrayList<>(branchCollector);
                                treeCollector.add(forkedCollector);
                                findRegisteredCommandTreeRecursively(treeCollector, forkedCollector, newNavigationNode, child);
                            }
                        });
                });

        }

        private static void removeSelfInCommandTree(@NotNull RegisteredCommandNode registeredCommandNode) {
            // NODE: Identify the `command node` by node name.
            @SuppressWarnings("unchecked")
            CommandNodeExtension<ServerCommandSource> parentNode = (CommandNodeExtension<ServerCommandSource>) registeredCommandNode.getParent();
            CommandNode<ServerCommandSource> childNode = registeredCommandNode.getNode();
            parentNode.fuji$getChildren().values().removeIf(it -> it.getName().equals(childNode.getName()));
            parentNode.fuji$getLiterals().values().removeIf(it -> it.getName().equals(childNode.getName()));
            parentNode.fuji$getArguments().values().removeIf(it -> it.getName().equals(childNode.getName()));
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
        return getDetailedCommandSyntax();
    }

    private @NotNull String getDetailedCommandSyntax() {
        return "/" + this.commandArguments
            .stream()
            .map(CommandArgument::toString)
            .collect(Collectors.joining(" "));
    }

    public @NotNull String getUserFriendlyCommandSyntax() {
        return "/" + this.commandArguments
            .stream()
            .filter(CommandArgument::isCommandArgumentSpecifier)
            .map(CommandArgument::toFriendlyString)
            .collect(Collectors.joining(" "));
    }

    @Override
    public String getSourceModule() {
        return ModulePathResolver.computeModulePathString(this.method.getDeclaringClass().getName());
    }

}
