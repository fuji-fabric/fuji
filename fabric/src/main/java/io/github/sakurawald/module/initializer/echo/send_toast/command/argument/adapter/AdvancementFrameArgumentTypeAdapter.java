package io.github.sakurawald.module.initializer.echo.send_toast.command.argument.adapter;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.github.sakurawald.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.core.command.argument.adapter.abst.BaseArgumentTypeAdapter;
import io.github.sakurawald.core.command.argument.structure.Argument;
import net.minecraft.advancement.AdvancementFrame;
import net.minecraft.server.command.ServerCommandSource;

import java.util.List;

public class AdvancementFrameArgumentTypeAdapter extends BaseArgumentTypeAdapter {

    @Override
    protected ArgumentType<?> makeArgumentType() {
        return StringArgumentType.string();
    }

    @Override
    public Object makeArgumentObject(CommandContext<ServerCommandSource> context, Argument argument) {
        return AdvancementFrame.valueOf(StringArgumentType.getString(context, argument.getArgumentName()));
    }

    @Override
    public List<Class<?>> getTypeClasses() {
        return List.of(AdvancementFrame.class);
    }

    @Override
    public List<String> getTypeStrings() {
        return List.of("advancement-frame");
    }

    @Override
    public RequiredArgumentBuilder<ServerCommandSource, ?> makeRequiredArgumentBuilder(String argumentName) {
        return super.makeRequiredArgumentBuilder(argumentName).suggests(CommandHelper.Suggestion.enums(AdvancementFrame::values));
    }
}
