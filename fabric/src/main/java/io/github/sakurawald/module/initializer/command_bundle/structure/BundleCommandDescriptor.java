package io.github.sakurawald.module.initializer.command_bundle.structure;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.ParsedArgument;
import com.mojang.brigadier.context.StringRange;
import io.github.sakurawald.core.auxiliary.LogUtil;
import io.github.sakurawald.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.core.command.argument.adapter.abst.BaseArgumentTypeAdapter;
import io.github.sakurawald.core.command.argument.structure.Argument;
import io.github.sakurawald.core.command.executor.CommandExecutor;
import io.github.sakurawald.core.command.structure.CommandDescriptor;
import io.github.sakurawald.core.command.structure.CommandRequirementDescriptor;
import io.github.sakurawald.core.command.structure.ExtendedCommandSource;
import io.github.sakurawald.module.initializer.command_bundle.accessor.CommandContextAccessor;
import lombok.Getter;
import lombok.SneakyThrows;
import net.minecraft.server.command.ServerCommandSource;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
 * 1. For a bundle command, there is no need to verify the command source: the verification is done by the end commands.
 * 2. A bundle command is type-safe for input, and type-unsafe for output.
 *
 * */
public class BundleCommandDescriptor extends CommandDescriptor {

    /* DSL definition */
    @SuppressWarnings({"RegExpRedundantEscape", "RegExpSimplifiable"})
    private static final Pattern BUNDLE_COMMAND_DSL = Pattern.compile("([<](\\S+)\\s+(\\S+)[>])|(\\[(\\S+)\\s+(\\S+)\\s?([\\s\\S]*?)\\])|(\\S+)");
    private static final int LEXEME_GROUP_INDEX = 0;
    private static final int REQUIRED_NON_OPTIONAL_ARGUMENT_TYPE_GROUP_INDEX = 2;
    private static final int REQUIRED_NON_OPTIONAL_ARGUMENT_NAME_GROUP_INDEX = 3;
    private static final int REQUIRED_OPTIONAL_ARGUMENT_TYPE_GROUP_INDEX = 5;
    private static final int REQUIRED_OPTIONAL_ARGUMENT_NAME_GROUP_INDEX = 6;
    private static final int REQUIRED_OPTIONAL_ARGUMENT_DEFAULT_VALUE_GROUP_INDEX = 7;
    private static final int LITERAL_ARGUMENT_NAME_GROUP_INDEX = 8;
    private static final String ARGUMENT_NAME_PLACEHOLDER = "$";

    /* global environment */
    final BundleCommandNode entry;
    @Getter
    final Map<String, String> optionalArgumentName2DefaultValue;

    private BundleCommandDescriptor(Method method, List<Argument> arguments, BundleCommandNode entry, Map<String, String> optionalArgumentName2DefaultValue) {
        super(method, arguments);
        this.entry = entry;
        this.optionalArgumentName2DefaultValue = optionalArgumentName2DefaultValue;
    }

    @SneakyThrows
    private static Method getFunctionClosure() {
        Method functionClosure = BundleCommandDescriptor.class.getDeclaredMethod("executeBundleCommandClosure"
            , CommandContext.class
            , BundleCommandDescriptor.class
            , List.class);
        functionClosure.setAccessible(true);

        return functionClosure;
    }

    private static int executeBundleCommandClosure(
        @NotNull CommandContext<ServerCommandSource> ctx
        , @NotNull BundleCommandDescriptor descriptor
        , @NotNull List<Object> args) {

        /* log */
        LogUtil.debug("the closure for `bundle command` associated with {} is invoked with args: ", descriptor.entry);
        args.forEach(arg -> LogUtil.debug("arg: {}", arg));

        /* execute with context */
        List<String> commands = new ArrayList<>(descriptor.entry.getBundle());

        Map<String, String> variables = new HashMap<>();

        /* fill the variables */
        int argumentIndex = 0;
        for (Argument argument : descriptor.arguments) {
            if (argument.isLiteralArgument()) continue;

            String argumentName = argument.getArgumentName();
            String argumentValue = (String) args.get(argumentIndex);
            variables.put(argumentName, argumentValue);
            argumentIndex++;
        }
        LogUtil.debug("fill the variables with: {}", variables);

        /* substitute the variables */
        commands = commands.stream().map(command -> {
            String newCommand = command;
            for (Map.Entry<String, String> variable : variables.entrySet()) {
                String oldStr = ARGUMENT_NAME_PLACEHOLDER + variable.getKey();
                @NotNull String newStr = variable.getValue();
                newCommand = newCommand.replace(oldStr, newStr);
            }
            return newCommand;
        }).toList();

        /* substitute the placeholders */
        ServerCommandSource source = ctx.getSource();
        commands = commands.stream().map(command -> TextHelper.parsePlaceholder(source, command)).toList();

        /* execute the commands */
        LogUtil.debug("execute bundle command: {}", commands);
        CommandExecutor.execute(ExtendedCommandSource.asConsole(source), commands);

        return 1;
    }

    public static BundleCommandDescriptor make(BundleCommandNode entry) {
        /* make arguments */
        List<Argument> arguments = new ArrayList<>();
        Map<String, String> defaultValueForOptionalArguments = new HashMap<>();

        String pattern = entry.getPattern();
        CommandRequirementDescriptor requirement = entry.getRequirement();

        Matcher matcher = BUNDLE_COMMAND_DSL.matcher(pattern);
        int argumentIndex = 0;
        while (matcher.find()) {

            if (matchLiteralArgument(matcher)) {
                String argumentName = matcher.group(LITERAL_ARGUMENT_NAME_GROUP_INDEX);
                arguments.add(Argument.makeLiteralArgument(argumentName, requirement));
            } else {
                boolean isOptional = matcher.group(LEXEME_GROUP_INDEX).startsWith("[");
                if (isOptional) {
                    String argumentType = matcher.group(REQUIRED_OPTIONAL_ARGUMENT_TYPE_GROUP_INDEX);
                    String argumentName = matcher.group(REQUIRED_OPTIONAL_ARGUMENT_NAME_GROUP_INDEX);
                    Class<?> type = BaseArgumentTypeAdapter.toTypeClass(argumentType);
                    arguments.add(Argument.makeRequiredArgument(type, argumentName, true, requirement));

                    // put default value for optional argument
                    String defaultValue = matcher.group(REQUIRED_OPTIONAL_ARGUMENT_DEFAULT_VALUE_GROUP_INDEX);
                    if (defaultValue == null) {
                        defaultValue = "";
                    }
                    defaultValueForOptionalArguments.put(argumentName, defaultValue);

                } else {
                    String argumentType = matcher.group(REQUIRED_NON_OPTIONAL_ARGUMENT_TYPE_GROUP_INDEX);
                    String argumentName = matcher.group(REQUIRED_NON_OPTIONAL_ARGUMENT_NAME_GROUP_INDEX);
                    Class<?> type = BaseArgumentTypeAdapter.toTypeClass(argumentType);
                    arguments.add(Argument.makeRequiredArgument(type, argumentName, false, requirement));
                }

            }

            argumentIndex++;
        }

        return new BundleCommandDescriptor(getFunctionClosure(), arguments, entry, defaultValueForOptionalArguments);
    }

    private static boolean matchLiteralArgument(Matcher matcher) {
        return matcher.group(LITERAL_ARGUMENT_NAME_GROUP_INDEX) != null;
    }

    @Override
    protected List<Object> makeObjectsByArguments(CommandContext<ServerCommandSource> ctx) {
        List<Object> args = new ArrayList<>();

        CommandContextAccessor<?> ctxAccessor = (CommandContextAccessor<?>) ctx;
        for (Argument argument : this.collectArgumentsToMakeObjects()) {
            String argumentName = argument.getArgumentName();

            /* collect the matched lexeme. */
            String arg;
            ParsedArgument<?, ?> parsedArgument = ctxAccessor.fuji$getArguments().get(argumentName);
            if (parsedArgument != null) {
                StringRange range = parsedArgument.getRange();
                arg = ctx.getInput().substring(range.getStart(), range.getEnd());
            } else {
                // if the optional argument is not specified, it will be null.
                arg = optionalArgumentName2DefaultValue.get(argumentName);
            }

            args.add(arg);
        }

        LogUtil.debug("make args for bundle command: {}", args);
        return args;
    }

    @SuppressWarnings("DuplicatedCode")
    @Override
    protected Command<ServerCommandSource> makeCommandFunctionClosure() {
        return (ctx) -> {

            /* invoke the command function */
            BundleCommandDescriptor descriptor = this;
            List<Object> args = makeObjectsByArguments(ctx);

            int value;
            try {
                value = (int) this.method.invoke(null, ctx, descriptor, args);
            } catch (Exception e) {
                return handleException(ctx, this.method, e);
            }

            return value;
        };
    }
}
