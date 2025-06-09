package io.github.sakurawald.core.command.argument.adapter.impl;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import io.github.sakurawald.core.command.argument.adapter.abst.BaseArgumentTypeAdapter;
import io.github.sakurawald.core.command.argument.structure.Argument;
import net.minecraft.command.argument.Vec2ArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.math.Vec2f;

import java.util.List;

public class Vec2ArgumentTypeAdapter extends BaseArgumentTypeAdapter {
    @Override
    protected ArgumentType<?> makeArgumentType() {
        return Vec2ArgumentType.vec2();
    }

    @Override
    protected Object makeArgumentObject(CommandContext<ServerCommandSource> context, Argument argument) {
        return Vec2ArgumentType.getVec2(context, argument.getArgumentName());
    }

    @Override
    public List<Class<?>> getTypeClasses() {
        return List.of(Vec2f.class);
    }

    @Override
    public List<String> getTypeStrings() {
        return List.of("vec2f", "vec2");
    }
}
