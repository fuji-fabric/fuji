package io.github.sakurawald.fuji.core.command.structure;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.PlayerHelper;
import io.github.sakurawald.fuji.core.command.argument.structure.CommandArgument;
import io.github.sakurawald.fuji.core.document.annotation.DocStringProvider;
import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.core.auxiliary.LogUtil;
import io.github.sakurawald.fuji.core.auxiliary.ReflectionUtil;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.LuckpermsHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.fuji.core.command.argument.adapter.abst.BaseArgumentTypeAdapter;
import io.github.sakurawald.fuji.core.command.exception.AbortCommandExecutionException;
import io.github.sakurawald.fuji.core.command.processor.CommandAnnotationProcessor;
import io.github.sakurawald.fuji.core.document.annotation.ForDeveloper;
import io.github.sakurawald.fuji.core.manager.impl.module.ModuleManager;
import io.github.sakurawald.fuji.core.document.descriptor.PermissionDescriptor;
import io.github.sakurawald.fuji.core.document.interfaces.SourceModuleGetter;
import lombok.RequiredArgsConstructor;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@ForDeveloper("""
    A command descriptor is used to describe a command instance.
    """)
public class CommandDescriptor implements SourceModuleGetter {

    public final @NotNull Method method;

    public final @NotNull List<CommandArgument> commandArguments;

    private @Nullable LiteralArgumentBuilder<ServerCommandSource> registerReturnValue;

    public @Nullable String document;

    public CommandDescriptor fillDocument(@Nullable Document document) {
        if (document == null) return this;
        return this.fillDocument(document.value());
    }

    public CommandDescriptor fillDocument(@Nullable String document) {
        if (document == null) return this;
        this.document = document;
        return this;
    }

    private static LiteralArgumentBuilder<ServerCommandSource> makeLiteralArgumentBuilder(@NotNull CommandArgument commandArgument) {
        return CommandManager.literal(commandArgument.getArgumentName());
    }

    private static RequiredArgumentBuilder<ServerCommandSource, ?> makeRequiredArgumentBuilder(@NotNull CommandArgument commandArgument) {
        /* use adapter to make the required argument builder */
        return BaseArgumentTypeAdapter.Registry.getTypeAdapter(commandArgument.getArgumentType()).makeRequiredArgumentBuilder(commandArgument.getArgumentName());
    }

    @SuppressWarnings("unchecked")
    private static LiteralArgumentBuilder<ServerCommandSource> makeRootArgumentBuilder(List<ArgumentBuilder<ServerCommandSource, ?>> builders, Command<ServerCommandSource> command) {
        ArgumentBuilder<ServerCommandSource, ?> root = null;

        for (int i = builders.size() - 1; i >= 0; i--) {
            ArgumentBuilder<ServerCommandSource, ?> node = builders.get(i);
            if (root == null) {
                root = node;
                root = root.executes(command);
                continue;
            }
            root = node.then(root);
        }

        // the command dispatcher only accepts the LiteralArgumentBuilder for register()
        if (!(root instanceof LiteralArgumentBuilder)) {
            throw new IllegalArgumentException("The root argument builder must be a literal argument builder.");
        }

        return (LiteralArgumentBuilder<ServerCommandSource>) root;
    }

    @DocStringProvider(id = 1751999362278L, value = "The permission used as the default string permission, for a command descriptor.")
    @SuppressWarnings("RedundantIfStatement")
    private static void setRequirementForArgumentBuilder(@NotNull ArgumentBuilder<ServerCommandSource, ?> builder, @Nullable CommandRequirementDescriptor requirement) {
        // don't override the command requirement if the annotation is null
        if (requirement == null) return;

        /* make the predicate */
        Predicate<ServerCommandSource> predicate = (ctx) -> {
            ServerPlayerEntity player = ctx.getPlayer();
            if (player == null) return true;
            if (requirement.getString() != null
                && !requirement.getString().isEmpty()
                && LuckpermsHelper.hasPermission(player.getUuid(), new PermissionDescriptor(requirement.getString(), 1751999362278L)))
                return true;
            if (ctx.hasPermissionLevel(requirement.getLevel())) return true;

            return false;
        };

        /* set the predicate */
        builder.requires(predicate);
    }

    private static String getCommandNodePath(CommandNode<ServerCommandSource> node) {
        StringBuilder sb = new StringBuilder();
        sb.append(node.getName());
        node.getChildren().forEach(child -> sb.append(".").append(getCommandNodePath(child)));
        return sb.toString();
    }

    private static boolean unregister(CommandNode<ServerCommandSource> targetNode, CommandNode<ServerCommandSource> navigationNode) {
        /* check npe */
        if (targetNode == null) return true;

        /* go down */
        navigationNode.getChildren()
            .stream()
            .toList()
            .forEach(child -> {
                if (unregister(targetNode.getChild(child.getName()), child)) {
                    // identify by argument name
                    targetNode.getChildren().removeIf(it -> it.getName().equals(child.getName()));
                }
            });

        /* remove leaf node */
        return targetNode.getChildren() == null
                || targetNode.getChildren().isEmpty();
    }

    private static CommandNode<ServerCommandSource> computeRedirectTargetOfOptionalArgument(List<CommandArgument> commandArguments) {
        List<String> prefix = commandArguments.stream()
            .filter(arg -> !arg.isCommandSource())
            .takeWhile(arg -> !arg.isOptional())
            .map(CommandArgument::getArgumentName)
            .toList();

        return CommandAnnotationProcessor.COMMAND_DISPATCHER.findNode(prefix);
    }

    private static List<ArgumentBuilder<ServerCommandSource, ?>> makeArgumentBuilders(CommandDescriptor descriptor) {
        List<ArgumentBuilder<ServerCommandSource, ?>> builders = new ArrayList<>();
        descriptor.commandArguments
            .stream()
            .filter(
                it ->
                    // ignore the optional arguments, since we will process them in the second pass.
                    !it.isOptional()
                        // ignore the command source arguments, the command source value is directly inject into the method invoke, should not register it in game.
                        && !it.isCommandSource())
            .forEach(argument -> {
                // make the builder
                ArgumentBuilder<ServerCommandSource, ?> builder = makeArgumentBuilder(argument);

                // set requirement specified by the argument for the builder
                setRequirementForArgumentBuilder(builder, argument.getRequirement());

                // add the builder
                builders.add(builder);
            });

        return builders;
    }

    private static ArgumentBuilder<ServerCommandSource, ?> makeArgumentBuilder(CommandArgument commandArgument) {
        ArgumentBuilder<ServerCommandSource, ?> builder;
        if (commandArgument.isRequiredArgument()) {
            builder = makeRequiredArgumentBuilder(commandArgument);
        } else {
            builder = makeLiteralArgumentBuilder(commandArgument);
        }
        return builder;
    }

    @SuppressWarnings("SameReturnValue")
    protected static int handleCommandException(CommandContext<ServerCommandSource> ctx, Method method, Exception wrappedOrUnwrappedException) {
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

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    protected static boolean verifyCommandSource(CommandContext<ServerCommandSource> ctx, CommandDescriptor descriptor) {
        List<CommandArgument> expectedCommandSources = descriptor.commandArguments
            .stream()
            .filter(CommandArgument::isCommandSource)
            .toList();

        // yeah, any type of source can use it.
        if (expectedCommandSources.isEmpty()) return true;
        // oh no, specify too many command sources.
        if (expectedCommandSources.size() > 1)
            throw new IllegalArgumentException("Expected only one command source: " + descriptor);

        return BaseArgumentTypeAdapter.Registry.getTypeAdapter(expectedCommandSources.get(0).getArgumentType()).verifyCommandSource(ctx);
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

    public String getCommandNodePath() {
        assert this.registerReturnValue != null;
        return getCommandNodePath(this.registerReturnValue.build());
    }

    public void unregister() {
        LogUtil.debug("Un-register command: {}", this);

        RootCommandNode<ServerCommandSource> root = CommandAnnotationProcessor.COMMAND_DISPATCHER.getRoot();

        assert this.registerReturnValue != null;
        LiteralCommandNode<ServerCommandSource> navigationNode = this.registerReturnValue.build();
        CommandNode<ServerCommandSource> targetNode = root.getChild(navigationNode.getName());
        if (targetNode != null) {
            if (CommandDescriptor.unregister(targetNode, navigationNode)) {
                root.getChildren().removeIf(p -> p.getName().equals(navigationNode.getName()));
            }
        }

        // sync the registry
        CommandAnnotationProcessor.REGISTERED_COMMAND_DESCRIPTORS.remove(this);
    }

    protected List<CommandArgument> collectArgumentsToMakeObjects() {
        return this.commandArguments
            .stream()
            /* filter out the literal command node and root command node. */
            .filter(CommandArgument::isRequiredArgument)
            .toList();
    }

    protected List<Object> makeObjectsByArguments(CommandContext<ServerCommandSource> ctx) {
        List<Object> args = new ArrayList<>();

        for (CommandArgument commandArgument : this.collectArgumentsToMakeObjects()) {
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

                // Throw other exceptions for upper-level handler.
                throw e;
            }

        }

        return args;
    }

    protected @NotNull Command<ServerCommandSource> makeCommandAction() {
        return (commandContext) -> {

            int value;
            try {
                /* verify command source */
                if (!verifyCommandSource(commandContext, this)) {
                    return CommandHelper.Return.FAIL;
                }

                /* invoke the command function */
                List<Object> args = makeObjectsByArguments(commandContext);
                value = (int) this.method.invoke(null, args.toArray());
            } catch (Exception wrappedOrUnwrappedException) {
                return handleCommandException(commandContext, this.method, wrappedOrUnwrappedException);
            }

            return value;
        };
    }

    public LiteralArgumentBuilder<ServerCommandSource> register() {
        LogUtil.debug("Register command: {}", this);

        /* first pass */
        var root = registerNonOptionalArguments();

        /* second pass */
        registerOptionalArguments();

        /* fill the props */
        this.registerReturnValue = root;

        /* sync the registry */
        CommandAnnotationProcessor.REGISTERED_COMMAND_DESCRIPTORS.add(this);
        return root;
    }

    @SuppressWarnings("UnusedReturnValue")
    private LiteralArgumentBuilder<ServerCommandSource> registerNonOptionalArguments() {
        /* make root builder */
        List<ArgumentBuilder<ServerCommandSource, ?>> builders = makeArgumentBuilders(this);
        Command<ServerCommandSource> command = makeCommandAction();
        LiteralArgumentBuilder<ServerCommandSource> root = makeRootArgumentBuilder(builders, command);

        /* register it */
        CommandAnnotationProcessor.COMMAND_DISPATCHER.register(root);

        return root;
    }

    private void registerOptionalArguments() {
        CommandNode<ServerCommandSource> redirectTargetNode = computeRedirectTargetOfOptionalArgument(this.commandArguments);

        this.commandArguments.stream()
            .filter(CommandArgument::isOptional)
            .forEach(optionalArgument -> {
                /* make it */
                ArgumentBuilder<ServerCommandSource, ?> optionalArgumentBuilder =
                    CommandManager
                        .literal("--" + optionalArgument.getArgumentName())
                        .then(makeRequiredArgumentBuilder(optionalArgument).executes(redirectTargetNode.getCommand()).redirect(redirectTargetNode));

                /* register it */
                redirectTargetNode.addChild(optionalArgumentBuilder.build());
            });
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
        for (CommandArgument commandArgument : this.commandArguments) {
            if (!commandArgument.isCommandSource()) continue;

            return commandArgument.getArgumentType().equals(CommandContext.class)
                || commandArgument.getArgumentType().equals(ServerCommandSource.class);
        }

        return true;
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
