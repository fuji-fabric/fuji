package mod.fuji.core.command.argument.structure;

import mod.fuji.core.command.argument.wrapper.impl.GreedyString;
import mod.fuji.core.command.argument.wrapper.impl.GreedyStringList;
import mod.fuji.core.command.structure.CommandActor;
import mod.fuji.core.document.annotation.Document;
import mod.fuji.core.command.annotation.CommandSource;
import mod.fuji.core.command.annotation.CommandTarget;
import mod.fuji.core.command.structure.CommandRequirementDescriptor;
import mod.fuji.core.document.annotation.ForDeveloper;
import lombok.Data;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Parameter;

@ForDeveloper("""
    A `CommandArgument` is used to describe a `parameter` declared in the command method.
    It's used to hold the bits.
    It's the component of a CommandDescriptor.

    ◉ Rules:
    - There are 2 kinds of Argument: LiteralArgument and RequiredArgument.
    - The treatment of RequiredArgument is the same as the LiteralArgument, except the GreedyStringArgument.
    - The GreedyStringArgument should always be the last parameter declared in the method.
    - An optional argument is a RequiredArgument, with command node redirects.
    - The parameter names in a method annotated with @CommandNode is a part of the command path, be careful to refactor these parameter names.
    - To introduce a new method parameter:
      1. If it's a non-optional parameter, you should add it as the last one in non-optional parameters.
      2. If it's an optional parameter, you can add it at any position in optional parameters.
      3. If it's a GreedyString parameter, you should add it as the last method parameter.
    """)
@Data
public class CommandArgument {

    private static final Class<?> LITERAL_ARGUMENT_TYPE_CLASS = Void.class;

    final @NotNull Class<?> argumentType;
    final @NotNull String argumentName;
    final boolean isOptional;
    @Nullable CommandRequirementDescriptor requirement;

    boolean isCommandSource;

    @ForDeveloper("This field is used for RetargetCommandDescriptor.")
    boolean isCommandTarget;

    @Nullable String document;

    public CommandArgument(@NotNull Class<?> argumentType, @NotNull String argumentName, boolean isOptional, @Nullable CommandRequirementDescriptor requirement) {
        this.argumentType = argumentType;
        this.argumentName = argumentName;
        this.isOptional = isOptional;
        this.requirement = requirement;
    }

    public static @NotNull CommandArgument ofRequiredArgument(@NotNull Class<?> typeClass, @NotNull String argumentName, boolean isOptional, @Nullable CommandRequirementDescriptor requirement) {
        return new CommandArgument(typeClass, argumentName, isOptional, requirement);
    }

    public static @NotNull CommandArgument ofLiteralArgument(@NotNull String argumentName, @Nullable CommandRequirementDescriptor requirement) {
        return new CommandArgument(LITERAL_ARGUMENT_TYPE_CLASS, argumentName, false, requirement);
    }

    public boolean isRequiredArgument() {
        return !isLiteralArgument();
    }

    public boolean isMethodParameterSpecifier() {
        return isRequiredArgument();
    }

    @SuppressWarnings("RedundantIfStatement")
    public boolean isCommandArgumentSpecifier() {
        // Ignore the command source argument, the command source value is directly injected into the method arguments, should not register it in the command tree.
        if (this.isCommandSource()) {
            return false;
        }

        // Ignore the command actor argument, its value is directly injected into the method arguments, if needed.
        if (this.getArgumentType().equals(CommandActor.class)) {
            return false;
        }

        return true;
    }


    public boolean isLiteralArgument() {
        return this.argumentType == LITERAL_ARGUMENT_TYPE_CLASS;
    }

    private @NotNull String toRequirementString() {
        if (this.requirement == null) {
            return "NONE";
        }

        return "{%d %s}"
            .formatted(this.requirement.getLevel(), this.requirement.getString())
            .trim();
    }

    @Override
    public String toString() {
        /* For required argument. */
        if (this.isRequiredArgument()) {
            String flags = "";
            if (this.isCommandSource) flags += "S";
            if (this.isCommandTarget) flags += "T";
            if (this.isOptional) {
                return "[%s %s]{flags=%s req=%s}".formatted(this.argumentType.getSimpleName(), this.argumentName, flags, this.toRequirementString());
            } else {
                return "<%s %s>{flags=%s req=%s}".formatted(this.argumentType.getSimpleName(), this.argumentName, flags, this.toRequirementString());
            }
        }

        /* For literal argument. */
        return "%s{req=%s}".formatted(this.argumentName, this.toRequirementString());
    }

    public @NotNull String toFriendlyString() {
        if (this.isLiteralArgument()) {
            return this.argumentName;
        }

        String argumentType = this.getArgumentType().getSimpleName();
        if (this.isOptional) {
            return "[%s %s]".formatted(argumentType, this.argumentName);
        } else {
            return "<%s %s>".formatted(argumentType, this.argumentName);
        }
    }

    public @NotNull CommandArgument fillParameter(@NotNull Parameter parameter) {
        this.fillDocument(parameter);
        this.fillCommandSource(parameter);
        this.fillCommandTarget(parameter);
        return this;
    }

    @SuppressWarnings("UnusedReturnValue")
    private @NotNull CommandArgument fillDocument(@NotNull Parameter parameter) {
        Document document = parameter.getAnnotation(Document.class);
        if (document == null) return this;

        this.document = document.value();
        return this;
    }

    @SuppressWarnings("UnusedReturnValue")
    private @NotNull CommandArgument fillCommandSource(@NotNull Parameter parameter) {
        if (!parameter.isAnnotationPresent(CommandSource.class)) return this;
        if (!this.isRequiredArgument()) {
            throw new IllegalArgumentException("The argument used as the command source must be a required argument.");
        }

        this.isCommandSource = true;
        return this;
    }

    @SuppressWarnings("UnusedReturnValue")
    private @NotNull CommandArgument fillCommandTarget(@NotNull Parameter parameter) {
        if (!parameter.isAnnotationPresent(CommandTarget.class)) return this;
        if (!this.isRequiredArgument()){
            throw new IllegalArgumentException("The argument used as the command target must be a required argument.");
        }
        if (!parameter.getType().equals(ServerPlayerEntity.class)) {
            throw new IllegalArgumentException("The @CommandTarget annotation can only be annotated on the ServerPlayerEntity parameter type: class = %s, method = %s".formatted(parameter.getDeclaringExecutable().getName(), parameter.getDeclaringExecutable().getDeclaringClass().getName()));
        }

        this.isCommandTarget = true;
        return this;
    }

    public boolean isGreedyArgumentType() {
        return this.argumentType.equals(GreedyString.class)
            || this.argumentType.equals(GreedyStringList.class);
    }

}
