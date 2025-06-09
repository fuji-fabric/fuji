package io.github.sakurawald.core.command.argument.adapter.impl;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import io.github.sakurawald.core.command.argument.adapter.abst.BaseArgumentTypeAdapter;
import io.github.sakurawald.core.command.argument.structure.Argument;
import net.minecraft.command.argument.PosArgument;
import net.minecraft.command.argument.RotationArgumentType;
import net.minecraft.server.command.ServerCommandSource;

import java.util.List;

public class RotationArgumentTypeAdapter extends BaseArgumentTypeAdapter {

    @Override
    protected ArgumentType<?> makeArgumentType() {
        return RotationArgumentType.rotation();
    }

    @Override
    protected Object makeArgumentObject(CommandContext<ServerCommandSource> context, Argument argument) {
        return RotationArgumentType.getRotation(context, argument.getArgumentName());
    }

    @Override
    public List<Class<?>> getTypeClasses() {
        return List.of(PosArgument.class);
    }

    @Override
    public List<String> getTypeStrings() {
        return List.of("rotation");
    }
}
