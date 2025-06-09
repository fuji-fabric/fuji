package io.github.sakurawald.core.command.argument.structure;

import io.github.sakurawald.core.annotation.Document;
import io.github.sakurawald.core.command.annotation.CommandSource;
import io.github.sakurawald.core.command.annotation.CommandTarget;
import io.github.sakurawald.core.command.structure.CommandRequirementDescriptor;
import lombok.Getter;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Parameter;

/**
 * Rules:
 * - There are 2 kinds of Argument: LiteralArgument and RequiredArgument.
 * - The treatment of RequiredArgument is the same as the LiteralArgument, except the GreedyStringArgument.
 * - The GreedyStringArgument should always be the last parameter written in the method.
 * - An optional argument is a RequiredArgument.
 * - The parameter names in a method annotated with @CommandNode is a part of the command path, be careful to refactor these parameter names.
 */
@Getter
public class Argument {
    private static final int THE_METHOD_PARAMETER_INDEX_FOR_LITERAL_ARGUMENT = -1;

    final @Nullable Class<?> type;
    final @NotNull String argumentName;
    final boolean isOptional;
    final @Nullable CommandRequirementDescriptor requirement;
    boolean isCommandSource;

    // this field is only used for RetargetCommandDescriptor
    boolean isCommandTarget;

    @Nullable String document;

    private Argument(@Nullable Class<?> type, @NotNull String argumentName, boolean isOptional, @Nullable CommandRequirementDescriptor requirement) {
        this.type = type;
        this.argumentName = argumentName;
        this.isOptional = isOptional;
        this.requirement = requirement;
    }

    public static Argument makeRequiredArgument(@NotNull Class<?> type, @NotNull String argumentName, boolean isOptional, @Nullable CommandRequirementDescriptor requirement) {
        return new Argument(type, argumentName, isOptional, requirement);
    }

    public static Argument makeLiteralArgument(@NotNull String argumentName, @Nullable CommandRequirementDescriptor requirement) {
        return new Argument(null, argumentName, false, requirement);
    }

    public Argument withDocument(@Nullable Document document) {
        if (document == null) return this;

        this.document = document.value();
        return this;
    }

    public boolean isRequiredArgument() {
        // the type for literal argument is always null.
        return this.type != null;
    }

    public boolean isLiteralArgument() {
        return !this.isRequiredArgument();
    }


    private String computeRequirementString() {
        if (this.requirement != null) {
            return "%d %s".formatted(this.requirement.getLevel(), this.requirement.getString())
                .trim();
        }

        return "";
    }

    @Override
    public String toString() {
        /* required argument */
        String flags = "";
        if (this.isCommandSource) flags += "S";
        if (this.isCommandTarget) flags += "T";

        if (this.isRequiredArgument()) {
            if (isOptional) {
                return "[%s](%s){%s}".formatted(this.argumentName, flags, this.computeRequirementString());
            } else {
                return "<%s>(%s){%s}".formatted(this.argumentName, flags, this.computeRequirementString());
            }
        }

        /* literal argument */
        return "%s{%s}".formatted(this.argumentName, this.computeRequirementString());
    }

    public String toHumanReadableString() {
        if (this.isLiteralArgument()) {
            return this.argumentName;
        }

        // the type is only null if this is a literal argument.
        assert this.getType() != null;
        if (isOptional) {
            return "[%s %s]".formatted(this.argumentName, this.getType().getSimpleName());
        } else {
            return "<%s %s>".formatted(this.argumentName, this.getType().getSimpleName());
        }
    }

    public Argument markWithParameter(Parameter parameter) {
        this.markAsCommandSourceWithParameter(parameter);
        this.markAsCommandTargetWithParameter(parameter);
        return this;
    }

    private Argument markAsCommandSourceWithParameter(Parameter parameter) {
        if (!parameter.isAnnotationPresent(CommandSource.class)) return this;

        if (!this.isRequiredArgument())
            throw new IllegalArgumentException("The argument for command source must be a required argument.");

        this.isCommandSource = true;
        return this;
    }

    private Argument markAsCommandTargetWithParameter(Parameter parameter) {
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
