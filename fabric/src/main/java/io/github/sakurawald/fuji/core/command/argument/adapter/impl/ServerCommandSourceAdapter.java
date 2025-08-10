package io.github.sakurawald.fuji.core.command.argument.adapter.impl;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import io.github.sakurawald.fuji.core.command.argument.adapter.abst.BaseArgumentTypeAdapter;
import io.github.sakurawald.fuji.core.command.argument.structure.Argument;
import net.minecraft.server.command.ServerCommandSource;

import java.util.Collections;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public class ServerCommandSourceAdapter extends BaseArgumentTypeAdapter {

    @Override
    protected ArgumentType<?> makeArgumentType() {
        throw new UnsupportedOperationException("You should add `@CommandSource` annotation before the CommandContext<ServerCommandSource> !");
    }

    @Override
    protected Object makeArgumentValue(@NotNull CommandContext<ServerCommandSource> context, @NotNull Argument argument) {
        return context.getSource();
    }

    @Override
    public List<Class<?>> getTypeClasses() {
        return List.of(ServerCommandSource.class);
    }

    @Override
    public List<String> getTypeNames() {
        return Collections.emptyList();
    }
}
