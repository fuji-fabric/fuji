package mod.fuji.module.initializer.command_bundle.structure;

import com.google.errorprone.annotations.Keep;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.ParsedArgument;
import com.mojang.brigadier.context.StringRange;
import mod.fuji.core.auxiliary.CollectionUtil;
import mod.fuji.core.auxiliary.LogUtil;
import mod.fuji.core.auxiliary.minecraft.CommandHelper;
import mod.fuji.core.auxiliary.minecraft.TextHelper;
import mod.fuji.core.command.argument.adapter.abst.BaseArgumentTypeAdapter;
import mod.fuji.core.command.argument.structure.CommandArgument;
import mod.fuji.core.command.executor.CommandExecutor;
import mod.fuji.core.command.descriptor.CommandDescriptor;
import mod.fuji.core.command.structure.CommandRequirementDescriptor;
import mod.fuji.core.command.executor.structure.ExtendedCommandSource;
import mod.fuji.module.initializer.command_bundle.accessor.CommandContextAccessor;
import java.util.Comparator;
import java.util.TreeMap;
import lombok.Getter;
import lombok.SneakyThrows;
import net.minecraft.commands.CommandSourceStack;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    /* Global environment */
    final BundleCommandNode entry;
    @Getter
    final Map<String, String> optionalArgumentName2DefaultValue;

    private BundleCommandDescriptor(@NotNull Method method, @NotNull List<CommandArgument> commandArguments, @NotNull BundleCommandNode entry, @NotNull Map<String, String> optionalArgumentName2DefaultValue) {
        super(method, commandArguments);
        this.entry = entry;
        this.optionalArgumentName2DefaultValue = optionalArgumentName2DefaultValue;
        this.fillDocument(entry.getDocument());
    }

    @Keep
    private static int bundleCommandGenericCommandMethod(@NotNull CommandContext<CommandSourceStack> commandContext, @NotNull BundleCommandDescriptor descriptor, @NotNull List<Object> variableValues) {
        LogUtil.debug("Execute bundle-command: definition = {}, variableValues = {}", descriptor.entry, variableValues);

        /* Define the variables. */
        // NOTE: Sort with the longest variable name first, to ensure the String#replace works properly.
        Map<String, String> variableTable = new TreeMap<>(Comparator
            .comparing(String::length)
            .reversed()
            .thenComparing(Comparator.naturalOrder()));

        int argumentIndex = 0;
        for (CommandArgument commandArgument : descriptor.commandArguments) {
            if (!commandArgument.isMethodParameterSpecifier()) continue;

            String argumentName = commandArgument.getArgumentName();
            String argumentValue = (String) variableValues.get(argumentIndex);
            variableTable.put(argumentName, argumentValue);
            argumentIndex++;
        }
        LogUtil.debug("Define the variable table: {}", variableTable);

        /* Resolve the user-defined variables. */
        List<String> commands = new ArrayList<>(descriptor.entry.getBundle());
        commands = commands.stream().map(command -> {
            String newCommand = command;
            for (Map.Entry<String, String> variable : variableTable.entrySet()) {
                String oldStr = ARGUMENT_NAME_PLACEHOLDER + variable.getKey();
                @NotNull String newStr = variable.getValue();
                newCommand = newCommand.replace(oldStr, newStr);
            }
            return newCommand;
        }).toList();

        /* Resolve the placeholders. */
        CommandSourceStack source = commandContext.getSource();
        commands = commands.stream()
            .map(command -> TextHelper.Parsers.parsePlaceholderString(source, command)).toList();

        /* Execute the commands. */
        LogUtil.debug("Execute bundle command: {}", commands);
        // NOTE: Use the last return value as the tree return value, so that a bundle command can be used to rewrite a predicate command.
        List<Integer> commandReturnValues = CommandExecutor.executeBatch(ExtendedCommandSource.asConsole(source), commands);
        return CollectionUtil
            .lastElement(commandReturnValues)
            .orElse(CommandHelper.Return.SUCCESS);
    }

    @Override
    protected @NotNull List<Object> makeMethodParameterValues(@NotNull CommandContext<CommandSourceStack> ctx) {
        List<Object> parameterValues = new ArrayList<>();

        CommandContextAccessor<?> ctxAccessor = (CommandContextAccessor<?>) ctx;
        for (CommandArgument commandArgument : this.getMethodParameterSpecifiers()) {
            String argumentName = commandArgument.getArgumentName();

            /* Collect the matched lexeme. */
            String lexeme;
            ParsedArgument<?, ?> parsedArgument = ctxAccessor.fuji$getArguments().get(argumentName);
            if (parsedArgument != null) {
                StringRange lexemeRange = parsedArgument.getRange();
                lexeme = ctx.getInput().substring(lexemeRange.getStart(), lexemeRange.getEnd());
            } else {
                // If the optional argument is not specified, it will be null.
                lexeme = optionalArgumentName2DefaultValue.get(argumentName);
            }

            parameterValues.add(lexeme);
        }

        LogUtil.debug("Make parameterValues for bundle command: {}", parameterValues);
        return parameterValues;
    }

    @SuppressWarnings("DuplicatedCode")
    @Override
    protected @NotNull Command<CommandSourceStack> makeCommandAction() {
        return withBaseCommandAction((commandContext) -> {
            BundleCommandDescriptor descriptor = this;
            List<Object> parameterValues = makeMethodParameterValues(commandContext);

            int commandReturnValue;
            try {
                commandReturnValue = (int) this.method.invoke(null, commandContext, descriptor, parameterValues);
            } catch (Exception e) {
                return CommandException.handleCommandExecutionException(commandContext, this.method, e);
            }

            return commandReturnValue;
        });
    }

    public static class Maker {

        public static @NotNull BundleCommandDescriptor from(@NotNull BundleCommandNode entry) {
            /* Make command arguments. */
            List<CommandArgument> commandArguments = new ArrayList<>();
            Map<String, String> defaultValueForOptionalArguments = new HashMap<>();

            String commandPattern = entry.getPattern();
            CommandRequirementDescriptor commandRequirement = entry.getRequirement();

            Matcher matcher = BUNDLE_COMMAND_DSL.matcher(commandPattern);
            while (matcher.find()) {
                if (matchLiteralArgument(matcher)) {
                    String argumentName = matcher.group(LITERAL_ARGUMENT_NAME_GROUP_INDEX);
                    commandArguments.add(CommandArgument.ofLiteralArgument(argumentName, commandRequirement));
                } else {
                    boolean isOptional = matcher.group(LEXEME_GROUP_INDEX).startsWith("[");
                    if (isOptional) {
                        String argumentTypeName = matcher.group(REQUIRED_OPTIONAL_ARGUMENT_TYPE_GROUP_INDEX);
                        String argumentName = matcher.group(REQUIRED_OPTIONAL_ARGUMENT_NAME_GROUP_INDEX);
                        Class<?> argumentTypeClass = BaseArgumentTypeAdapter.Registry.toTypeClass(argumentTypeName);
                        commandArguments.add(CommandArgument.ofRequiredArgument(argumentTypeClass, argumentName, true, commandRequirement));

                        // Remember the default value for this optional argument.
                        String defaultValue = matcher.group(REQUIRED_OPTIONAL_ARGUMENT_DEFAULT_VALUE_GROUP_INDEX);
                        if (defaultValue == null) {
                            defaultValue = "";
                        }
                        defaultValueForOptionalArguments.put(argumentName, defaultValue);

                    } else {
                        String argumentTypeName = matcher.group(REQUIRED_NON_OPTIONAL_ARGUMENT_TYPE_GROUP_INDEX);
                        String argumentName = matcher.group(REQUIRED_NON_OPTIONAL_ARGUMENT_NAME_GROUP_INDEX);
                        Class<?> argumentTypeClass = BaseArgumentTypeAdapter.Registry.toTypeClass(argumentTypeName);
                        commandArguments.add(CommandArgument.ofRequiredArgument(argumentTypeClass, argumentName, false, commandRequirement));
                    }
                }
            }

            return new BundleCommandDescriptor(getBundleCommandGenericCommandMethod(), commandArguments, entry, defaultValueForOptionalArguments);
        }

        @SneakyThrows(NoSuchMethodException.class)
        private static @NotNull Method getBundleCommandGenericCommandMethod() {
            Method bundleCommandGenericCommandMethod = BundleCommandDescriptor.class.getDeclaredMethod("bundleCommandGenericCommandMethod"
                , CommandContext.class
                , BundleCommandDescriptor.class
                , List.class);
            bundleCommandGenericCommandMethod.setAccessible(true);
            return bundleCommandGenericCommandMethod;
        }

        private static boolean matchLiteralArgument(@NotNull Matcher matcher) {
            return matcher.group(LITERAL_ARGUMENT_NAME_GROUP_INDEX) != null;
        }
    }
}
