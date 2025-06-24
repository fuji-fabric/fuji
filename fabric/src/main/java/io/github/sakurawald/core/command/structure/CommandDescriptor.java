package io.github.sakurawald.core.command.structure;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import io.github.sakurawald.core.annotation.Document;
import io.github.sakurawald.core.auxiliary.LogUtil;
import io.github.sakurawald.core.auxiliary.ReflectionUtil;
import io.github.sakurawald.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.core.auxiliary.minecraft.PermissionHelper;
import io.github.sakurawald.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.core.command.argument.adapter.abst.BaseArgumentTypeAdapter;
import io.github.sakurawald.core.command.argument.structure.Argument;
import io.github.sakurawald.core.command.exception.AbortCommandExecutionException;
import io.github.sakurawald.core.command.processor.CommandAnnotationProcessor;
import io.github.sakurawald.core.manager.impl.module.ModuleManager;
import io.github.sakurawald.core.structure.descriptor.PermissionDescriptor;
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

public class CommandDescriptor {

    public final Method method;

    public final List<Argument> arguments;

    // it's null if get before register()
    private @Nullable LiteralArgumentBuilder<ServerCommandSource> registerReturnValue;

    public @Nullable String document;

    public CommandDescriptor withDocument(@Nullable Document document) {
        if (document == null) return this;

        this.document = document.value();
        return this;
    }

    public CommandDescriptor(Method method, List<Argument> arguments) {
        this.method = method;
        this.arguments = arguments;
    }

    private static LiteralArgumentBuilder<ServerCommandSource> makeLiteralArgumentBuilder(Argument argument) {
        return CommandManager.literal(argument.getArgumentName());
    }

    private static RequiredArgumentBuilder<ServerCommandSource, ?> makeRequiredArgumentBuilder(Argument argument) {
        /* use adapter to make the required argument builder */
        return BaseArgumentTypeAdapter.getAdapter(argument.getType()).makeRequiredArgumentBuilder(argument.getArgumentName());
    }

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
                && PermissionHelper.hasPermission(player.getUuid(), new PermissionDescriptor(requirement.getString(), "Permission for a command descriptor.")))
                return true;
            if (ctx.hasPermissionLevel(requirement.getLevel())) return true;

            return false;
        };

        /* set the predicate */
        builder.requires(predicate);
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

    private static String getCommandNodePath(CommandNode<ServerCommandSource> node) {
        StringBuilder sb = new StringBuilder();
        sb.append(node.getName());
        node.getChildren().forEach(child -> sb.append(".").append(getCommandNodePath(child)));
        return sb.toString();
    }

    private static boolean unregister(
        CommandNode<ServerCommandSource> targetNode
        , CommandNode<ServerCommandSource> navigationNode
    ) {
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

    private static CommandNode<ServerCommandSource> computeRedirectTargetOfOptionalArgument(List<Argument> arguments) {
        List<String> prefix = arguments.stream()
            .filter(arg -> !arg.isCommandSource())
            .takeWhile(arg -> !arg.isOptional())
            .map(Argument::getArgumentName)
            .toList();

        return CommandAnnotationProcessor.getDispatcher().findNode(prefix);
    }

    private static List<ArgumentBuilder<ServerCommandSource, ?>> makeArgumentBuilders(CommandDescriptor descriptor) {
        List<ArgumentBuilder<ServerCommandSource, ?>> builders = new ArrayList<>();
        descriptor.arguments
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

    private static ArgumentBuilder<ServerCommandSource, ?> makeArgumentBuilder(Argument argument) {
        ArgumentBuilder<ServerCommandSource, ?> builder;
        if (argument.isRequiredArgument()) {
            builder = makeRequiredArgumentBuilder(argument);
        } else {
            builder = makeLiteralArgumentBuilder(argument);
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
        List<Argument> expectedCommandSources = descriptor.arguments
            .stream()
            .filter(Argument::isCommandSource)
            .toList();

        // yeah, any type of source can use it.
        if (expectedCommandSources.isEmpty()) return true;
        // oh no, specify too many command sources.
        if (expectedCommandSources.size() > 1)
            throw new IllegalArgumentException("Expected only one command source: " + descriptor);

        return BaseArgumentTypeAdapter.getAdapter(expectedCommandSources.get(0).getType()).verifyCommandSource(ctx);
    }

    protected static void reportException(ServerCommandSource source, Method method, Throwable throwable) {
        /* report to console */
        String string = """
            [Fuji Exception Catcher]
            - Source: %s
            - Module: %s
            - Method: %s
            - Message: %s

            """.formatted(
            source.getName()
            , ModuleManager.computeModulePathAsList(method.getDeclaringClass().getName())
            , method.getName()
            , throwable);
        LogUtil.error(string, throwable);

        /* report to command source */
        String stacktrace = String.join("\n", ReflectionUtil.getStackTraceAsList(throwable));
        MutableText report = TextHelper.getTextByValue(source, string)
            .copy()
            .setStyle(Style.EMPTY
                .withColor(CommandHelper.EXCEPTION_COLOR)
                .withHoverEvent(TextHelper.HoverEvent.makeShowTextAction(Text.of("Click to copy the stacktrace.")))
                .withClickEvent(TextHelper.ClickEvent.makeCopyToClipboardAction(stacktrace)));

        source.sendMessage(report);
    }

    public String getCommandNodePath() {
        assert this.registerReturnValue != null;
        return getCommandNodePath(this.registerReturnValue.build());
    }

    public void unregister() {
        LogUtil.debug("Un-register command: {}", this);

        RootCommandNode<ServerCommandSource> root = CommandAnnotationProcessor.getDispatcher().getRoot();

        assert this.registerReturnValue != null;
        LiteralCommandNode<ServerCommandSource> navigationNode = this.registerReturnValue.build();
        com.mojang.brigadier.tree.CommandNode<ServerCommandSource> targetNode = root.getChild(navigationNode.getName());
        if (targetNode != null) {
            if (CommandDescriptor.unregister(targetNode, navigationNode)) {
                root.getChildren().removeIf(p -> p.getName().equals(navigationNode.getName()));
            }
        }

        // sync the registry
        CommandAnnotationProcessor.descriptors.remove(this);
    }

    protected List<Argument> collectArgumentsToMakeObjects() {
        return this.arguments
            .stream()
            /* filter out the literal command node and root command node. */
            .filter(Argument::isRequiredArgument)
            .toList();
    }

    protected List<Object> makeObjectsByArguments(CommandContext<ServerCommandSource> ctx) {
        List<Object> args = new ArrayList<>();

        for (Argument argument : this.collectArgumentsToMakeObjects()) {
            /* inject the value into a required argument. */
            try {
                Object arg = BaseArgumentTypeAdapter
                    .getAdapter(argument.getType())
                    .makeParameterObject(ctx, argument);

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

                // throw other exception for upper-level handler
                throw e;
            }

        }

        return args;
    }

    protected Command<ServerCommandSource> makeCommandFunctionClosure() {
        return (ctx) -> {

            /* verify command source */
            if (!verifyCommandSource(ctx, this)) {
                return CommandHelper.Return.FAIL;
            }

            /* invoke the command function */
            List<Object> args = makeObjectsByArguments(ctx);

            int value;
            try {
                value = (int) this.method.invoke(null, args.toArray());
            } catch (Exception wrappedOrUnwrappedException) {
                return handleCommandException(ctx, this.method, wrappedOrUnwrappedException);
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
        CommandAnnotationProcessor.descriptors.add(this);
        return root;
    }

    @SuppressWarnings("UnusedReturnValue")
    private LiteralArgumentBuilder<ServerCommandSource> registerNonOptionalArguments() {
        /* make root builder */
        List<ArgumentBuilder<ServerCommandSource, ?>> builders = makeArgumentBuilders(this);
        Command<ServerCommandSource> command = makeCommandFunctionClosure();
        LiteralArgumentBuilder<ServerCommandSource> root = makeRootArgumentBuilder(builders, command);

        /* register it */
        CommandAnnotationProcessor.getDispatcher().register(root);

        return root;
    }

    private void registerOptionalArguments() {
        CommandNode<ServerCommandSource> redirectTargetNode = computeRedirectTargetOfOptionalArgument(this.arguments);

        this.arguments.stream()
            .filter(Argument::isOptional)
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

    @Override
    public String toString() {
        return "/" + this.arguments.stream().map(Argument::toString).collect(Collectors.joining(" "));
    }

    public String getCommandSyntax() {
        StringBuilder syntax = new StringBuilder()
            .append("/");

        this.arguments.stream()
            .filter(it -> !it.isCommandSource())
            .forEach(it -> syntax.append(it.toHumanReadableString()).append(" "));

        return syntax.toString();
    }

    public int getDefaultLevelPermission() {
        int minRequiredLevel = CommandRequirementDescriptor.getDefaultLevel();

        for (Argument argument : this.arguments) {
            if (argument.getRequirement() == null) continue;

            minRequiredLevel = Math.max(minRequiredLevel, argument.getRequirement().getLevel());
        }
        return minRequiredLevel;
    }

    public String getDefaultStringPermission() {
        String requiredString = CommandRequirementDescriptor.getDefaultString();
        for (Argument argument : this.arguments) {
            if (argument.getRequirement() == null) continue;

            String string = argument.getRequirement().getString();
            if (string != null && !string.isBlank()) {
                requiredString = string;
                break;
            }
        }

        return requiredString.isBlank() ? "none" : requiredString;
    }

    public boolean canBeExecutedByConsole() {
        for (Argument argument : this.arguments) {
            if (!argument.isCommandSource()) continue;

            assert argument.getType() != null;
            return argument.getType().equals(CommandContext.class)
                || argument.getType().equals(ServerCommandSource.class);
        }

        return true;
    }
}
