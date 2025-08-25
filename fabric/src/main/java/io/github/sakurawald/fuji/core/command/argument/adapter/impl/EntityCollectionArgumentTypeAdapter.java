package io.github.sakurawald.fuji.core.command.argument.adapter.impl;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import io.github.sakurawald.fuji.core.command.argument.adapter.abst.BaseArgumentTypeAdapter;
import io.github.sakurawald.fuji.core.command.argument.structure.CommandArgument;
import io.github.sakurawald.fuji.core.command.argument.wrapper.impl.EntityCollection;
import java.util.List;
import lombok.SneakyThrows;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import org.jetbrains.annotations.NotNull;

public class EntityCollectionArgumentTypeAdapter extends BaseArgumentTypeAdapter {
    @Override
    public List<Class<?>> getTypeClasses() {
        return List.of(EntityCollection.class);
    }

    @Override
    public List<String> getTypeNames() {
        return List.of("entities");
    }

    @Override
    protected ArgumentType<?> makeArgumentType() {
        return EntityArgumentType.entities();
    }

    @SneakyThrows
    @Override
    protected Object makeArgumentValue(@NotNull CommandContext<ServerCommandSource> context, @NotNull CommandArgument commandArgument) {
        return new EntityCollection(EntityArgumentType.getEntities(context, commandArgument.getArgumentName()));
    }
}
