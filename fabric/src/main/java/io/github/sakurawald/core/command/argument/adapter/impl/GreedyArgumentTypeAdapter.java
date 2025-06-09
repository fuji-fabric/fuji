package io.github.sakurawald.core.command.argument.adapter.impl;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import io.github.sakurawald.core.command.argument.adapter.abst.BaseArgumentTypeAdapter;
import io.github.sakurawald.core.command.argument.structure.Argument;
import io.github.sakurawald.core.command.argument.wrapper.impl.GreedyString;
import net.minecraft.server.command.ServerCommandSource;

import java.util.List;

public class GreedyArgumentTypeAdapter extends BaseArgumentTypeAdapter {

    @Override
    public ArgumentType<?> makeArgumentType() {
        return StringArgumentType.greedyString();
    }

    @Override
    public Object makeArgumentObject(CommandContext<ServerCommandSource> context, Argument argument) {
        return new GreedyString(StringArgumentType.getString(context, argument.getArgumentName()));
    }

    @Override
    public List<Class<?>> getTypeClasses() {
        return List.of(GreedyString.class);
    }

    @Override
    public List<String> getTypeStrings() {
        return List.of("greedy-string", "greedy");
    }
}
