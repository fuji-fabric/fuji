package io.github.sakurawald.core.command.argument.adapter.impl;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import io.github.sakurawald.core.command.argument.adapter.abst.BaseArgumentTypeAdapter;
import io.github.sakurawald.core.command.argument.structure.Argument;
import net.minecraft.server.command.ServerCommandSource;

import java.util.Collections;
import java.util.List;

public class ServerCommandSourceAdapter extends BaseArgumentTypeAdapter {

    @Override
    protected ArgumentType<?> makeArgumentType() {
        throw new UnsupportedOperationException("You should add `@CommandSource` annotation before the CommandContext<ServerCommandSource> !");
    }

    @Override
    protected Object makeArgumentObject(CommandContext<ServerCommandSource> context, Argument argument) {
        return context.getSource();
    }

    @Override
    public List<Class<?>> getTypeClasses() {
        return List.of(ServerCommandSource.class);
    }

    @Override
    public List<String> getTypeStrings() {
        return Collections.emptyList();
    }
}
