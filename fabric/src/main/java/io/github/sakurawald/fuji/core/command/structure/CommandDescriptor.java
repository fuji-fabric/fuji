package io.github.sakurawald.fuji.core.command.structure;

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
import io.github.sakurawald.fuji.core.auxiliary.minecraft.PlayerHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.fuji.core.command.argument.adapter.abst.BaseArgumentTypeAdapter;
import io.github.sakurawald.fuji.core.command.argument.structure.CommandArgument;
import io.github.sakurawald.fuji.core.command.exception.AbortCommandExecutionException;
import io.github.sakurawald.fuji.core.command.processor.CommandAnnotationProcessor;
import io.github.sakurawald.fuji.core.document.annotation.DocStringProvider;
import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.core.document.annotation.ForDeveloper;
import io.github.sakurawald.fuji.core.document.descriptor.PermissionDescriptor;
import io.github.sakurawald.fuji.core.document.interfaces.SourceModuleGetter;
import io.github.sakurawald.fuji.core.manager.impl.module.ModuleManager;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
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

    @DocStringProvider(id = 1751999362278L, value = "The permission used as the default string permission, for a command descriptor.")
    @SuppressWarnings("RedundantIfStatement")
    private static void fillCommandRequirement(@NotNull ArgumentBuilder<ServerCommandSource, ?> builder, @Nullable CommandRequirementDescriptor requirement) {
        // Don't override the command requirement if the annotation is null
        if (requirement == null) {
            return;
        }

        /* Make the predicate. */
        Predicate<ServerCommandSource> predicate = (ctx) -> {
            ServerPlayerEntity player = ctx.getPlayer();

            /* The console can use all commands. */
            if (player == null) return true;

            /* Check the string permission. */
            if (requirement.getString() != null
                && !requirement.getString().isEmpty()
                && LuckpermsHelper.hasPermission(player.getUuid(), new PermissionDescriptor(requirement.getString(), 1751999362278L))) {
                return true;
            }

            /* Check the level permission. */
            if (ctx.hasPermissionLevel(requirement.getLevel())) {
                return true;
            }

            /* Insufficient permission to use this command. */
            return false;
        };

        /* Set the predicate. */
        builder.requires(predicate);
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

    protected @NotNull List<CommandArgument> getParameterSpecifiers() {
        return this.commandArguments
            .stream()
            /* Filter out the literal command node and root command node. */
            .filter(CommandArgument::isRequiredArgument)
            .toList();
    }

    protected @NotNull List<Object> makeParameterValues(@NotNull CommandContext<ServerCommandSource> ctx) {
        List<Object> args = new ArrayList<>();

        for (CommandArgument commandArgument : this.getParameterSpecifiers()) {
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

    protected @NotNull Command<ServerCommandSource> makeCommandAction() {
        return (commandContext) -> {
            int commandReturnValue;
            try {
                /* Verify the command source. */
                if (!CommandSource.verifyCommandSource(commandContext, this)) {
                    return CommandHelper.Return.FAIL;
                }

                /* invoke the command function */
                List<Object> args = makeParameterValues(commandContext);
                commandReturnValue = (int) this.method.invoke(null, args.toArray());
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
                return CommandHelper.Return.FAIL;
            }

            /* report the exception */
            reportException(ctx.getSource(), method, theRealException);
            return CommandHelper.Return.FAIL;
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
            if (PlayerHelper.isAdmin(source)) {
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
            List<ArgumentBuilder<ServerCommandSource, ?>> builders = new ArrayList<>();
            descriptor.commandArguments
                .stream()
                .filter(
                    it ->
                        // Ignore the optional arguments, since we will process them in the second pass.
                        !it.isOptional()
                            // Ignore the command source arguments, the command source value is directly injected into the method arguments, should not register it in the command tree.
                            && !it.isCommandSource())
                .forEach(argument -> {
                    /* Make the argument builder. */
                    ArgumentBuilder<ServerCommandSource, ?> builder = makeArgumentBuilder(argument);

                    /* Fill the requirement for the builder. */
                    fillCommandRequirement(builder, argument.getRequirement());

                    /* Add the builder. */
                    builders.add(builder);
                });

            return builders;
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

    public int getDefaultLevelPermission() {
        int minRequiredLevel = CommandRequirementDescriptor.getDefaultLevel();

        for (CommandArgument commandArgument : this.commandArguments) {
            if (commandArgument.getRequirement() == null) continue;

            minRequiredLevel = Math.max(minRequiredLevel, commandArgument.getRequirement().getLevel());
        }
        return minRequiredLevel;
    }

    public String getDefaultStringPermission() {
        String requiredString = CommandRequirementDescriptor.getDefaultString();
        for (CommandArgument commandArgument : this.commandArguments) {
            if (commandArgument.getRequirement() == null) continue;

            String string = commandArgument.getRequirement().getString();
            if (string != null && !string.isBlank()) {
                requiredString = string;
                break;
            }
        }

        return requiredString.isBlank() ? "none" : requiredString;
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
            .filter(it -> !it.isCommandSource())
            .map(CommandArgument::toFriendlyString)
            .collect(Collectors.joining(" "));
    }

    @Override
    public String getSourceModule() {
        return ModuleManager.computeJoinedModulePath(this.method.getDeclaringClass().getName());
    }

}
