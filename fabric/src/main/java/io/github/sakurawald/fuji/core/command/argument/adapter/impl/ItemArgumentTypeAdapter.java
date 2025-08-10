package io.github.sakurawald.fuji.core.command.argument.adapter.impl;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import io.github.sakurawald.fuji.core.command.argument.adapter.abst.BaseArgumentTypeAdapter;
import io.github.sakurawald.fuji.core.command.argument.structure.CommandArgument;
import io.github.sakurawald.fuji.core.command.processor.CommandAnnotationProcessor;
import net.minecraft.command.argument.ItemStackArgumentType;
import net.minecraft.item.Item;
import net.minecraft.server.command.ServerCommandSource;

import java.util.List;
import org.jetbrains.annotations.NotNull;

public class ItemArgumentTypeAdapter extends BaseArgumentTypeAdapter {

    @Override
    protected ArgumentType<?> makeArgumentType() {
        return ItemStackArgumentType.itemStack(CommandAnnotationProcessor.COMMAND_REGISTRY_ACCESS);
    }

    @Override
    public Object makeArgumentValue(@NotNull CommandContext<ServerCommandSource> context, @NotNull CommandArgument commandArgument) {
        return ItemStackArgumentType.getItemStackArgument(context, commandArgument.getArgumentName()).getItem();
    }

    @Override
    public List<Class<?>> getTypeClasses() {
        return List.of(Item.class);
    }

    @Override
    public List<String> getTypeNames() {
        return List.of("item");
    }
}
