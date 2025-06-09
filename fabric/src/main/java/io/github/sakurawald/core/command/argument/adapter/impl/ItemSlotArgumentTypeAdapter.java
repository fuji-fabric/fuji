package io.github.sakurawald.core.command.argument.adapter.impl;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import io.github.sakurawald.core.command.argument.adapter.abst.BaseArgumentTypeAdapter;
import io.github.sakurawald.core.command.argument.structure.Argument;
import net.minecraft.command.argument.ItemSlotArgumentType;
import net.minecraft.server.command.ServerCommandSource;

import java.util.List;

public class ItemSlotArgumentTypeAdapter extends BaseArgumentTypeAdapter {
    @Override
    protected ArgumentType<?> makeArgumentType() {
        return ItemSlotArgumentType.itemSlot();
    }

    @Override
    protected Object makeArgumentObject(CommandContext<ServerCommandSource> context, Argument argument) {
        return ItemSlotArgumentType.getItemSlot(context, argument.getArgumentName());
    }

    @Override
    public List<Class<?>> getTypeClasses() {
        return List.of(ItemSlotArgumentType.class);
    }

    @Override
    public List<String> getTypeStrings() {
        return List.of("item-slot");
    }
}
