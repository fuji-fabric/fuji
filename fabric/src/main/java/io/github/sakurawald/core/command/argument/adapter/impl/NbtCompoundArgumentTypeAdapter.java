package io.github.sakurawald.core.command.argument.adapter.impl;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import io.github.sakurawald.core.command.argument.adapter.abst.BaseArgumentTypeAdapter;
import io.github.sakurawald.core.command.argument.structure.Argument;
import net.minecraft.command.argument.NbtCompoundArgumentType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.command.ServerCommandSource;

import java.util.List;

public class NbtCompoundArgumentTypeAdapter extends BaseArgumentTypeAdapter {
    @Override
    protected ArgumentType<?> makeArgumentType() {
        return NbtCompoundArgumentType.nbtCompound();
    }

    @Override
    protected Object makeArgumentObject(CommandContext<ServerCommandSource> context, Argument argument) {
        return NbtCompoundArgumentType.getNbtCompound(context, argument.getArgumentName());
    }

    @Override
    public List<Class<?>> getTypeClasses() {
        return List.of(NbtCompound.class);
    }

    @Override
    public List<String> getTypeStrings() {
        return List.of("nbt-compound");
    }
}
