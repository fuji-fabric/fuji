package io.github.sakurawald.fuji.core.manager.impl.bossbar.command.argument.adapter;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.fuji.core.command.argument.adapter.abst.BaseArgumentTypeAdapter;
import io.github.sakurawald.fuji.core.command.argument.structure.Argument;
import io.github.sakurawald.fuji.core.manager.impl.bossbar.command.argument.wrapper.StepType;
import net.minecraft.server.command.ServerCommandSource;

import java.util.List;
import org.jetbrains.annotations.NotNull;

public class StepTypeArgumentTypeAdapter extends BaseArgumentTypeAdapter {

    @Override
    protected ArgumentType<?> makeArgumentType() {
        return StringArgumentType.string();
    }

    @Override
    protected Object makeArgumentObject(@NotNull CommandContext<ServerCommandSource> context, @NotNull Argument argument) {
        String name = StringArgumentType.getString(context, argument.getArgumentName());
        return StepType.valueOf(name);
    }

    @Override
    public @NotNull RequiredArgumentBuilder<ServerCommandSource, ?> makeRequiredArgumentBuilder(@NotNull String argumentName) {
        return super.makeRequiredArgumentBuilder(argumentName)
            .suggests(CommandHelper.Suggestion.enums(StepType::values));
    }

    @Override
    public List<Class<?>> getTypeClasses() {
        return List.of(StepType.class);
    }

    @Override
    public List<String> getTypeStrings() {
        return List.of("step-type");
    }
}
