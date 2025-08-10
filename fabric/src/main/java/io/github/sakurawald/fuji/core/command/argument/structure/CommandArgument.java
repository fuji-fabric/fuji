package io.github.sakurawald.fuji.core.command.argument.structure;

import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.core.command.annotation.CommandSource;
import io.github.sakurawald.fuji.core.command.annotation.CommandTarget;
import io.github.sakurawald.fuji.core.command.structure.CommandRequirementDescriptor;
import io.github.sakurawald.fuji.core.document.annotation.ForDeveloper;
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
    """)
@Data
public class CommandArgument {

    private static final Class<?> LITERAL_ARGUMENT_TYPE_CLASS = Void.class;

    final @NotNull Class<?> argumentType;
    final @NotNull String argumentName;
    final boolean isOptional;
    final @Nullable CommandRequirementDescriptor requirement;

    boolean isCommandSource;

    @ForDeveloper("This field is used for RetargetCommandDescriptor.")
    boolean isCommandTarget;

    @Nullable String document;

    public static @NotNull CommandArgument ofRequiredArgument(@NotNull Class<?> typeClass, @NotNull String argumentName, boolean isOptional, @Nullable CommandRequirementDescriptor commandRequirement) {
        return new CommandArgument(typeClass, argumentName, isOptional, commandRequirement);
    }

    public static @NotNull CommandArgument ofLiteralArgument(@NotNull String argumentName, @Nullable CommandRequirementDescriptor requirement) {
        return new CommandArgument(LITERAL_ARGUMENT_TYPE_CLASS, argumentName, false, requirement);
    }

    public boolean isRequiredArgument() {
        return !isLiteralArgument();
    }

    public boolean isLiteralArgument() {
        return this.argumentType == LITERAL_ARGUMENT_TYPE_CLASS;
    }

    private @NotNull String toRequirementString() {
        if (this.requirement == null) {
            return "NONE";
        }

        return "%d %s"
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
                return "[%s]{flags=%s req=%s}".formatted(this.argumentName, flags, this.toRequirementString());
            } else {
                return "<%s>{flags=%s req=%s}".formatted(this.argumentName, flags, this.toRequirementString());
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

    public CommandArgument withDocument(@Nullable Document document) {
        if (document == null) return this;

        this.document = document.value();
        return this;
    }

    public CommandArgument markWithParameter(Parameter parameter) {
        this.markAsCommandSourceWithParameter(parameter);
        this.markAsCommandTargetWithParameter(parameter);
        return this;
    }

    private CommandArgument markAsCommandSourceWithParameter(Parameter parameter) {
        if (!parameter.isAnnotationPresent(CommandSource.class)) return this;

        if (!this.isRequiredArgument())
            throw new IllegalArgumentException("The argument for command source must be a required argument.");

        this.isCommandSource = true;
        return this;
    }

    private CommandArgument markAsCommandTargetWithParameter(Parameter parameter) {
        if (!parameter.isAnnotationPresent(CommandTarget.class)) return this;

        if (!this.isRequiredArgument())
            throw new IllegalArgumentException("The argument for command target must be a required argument.");

        if (!parameter.getType().equals(ServerPlayerEntity.class)) {
            throw new IllegalArgumentException("the annotation @CommandTarget can only be used in a parameter whose type is ServerPlayerEntity: class = %s, method = %s".formatted(parameter.getDeclaringExecutable().getName(), parameter.getDeclaringExecutable().getDeclaringClass().getSimpleName()));
        }

        this.isCommandTarget = true;
        return this;
    }

}
