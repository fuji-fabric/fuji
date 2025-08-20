package io.github.sakurawald.fuji.core.command.argument.adapter.impl;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.RegistryHelper;
import io.github.sakurawald.fuji.core.command.argument.adapter.abst.BaseArgumentTypeAdapter;
import io.github.sakurawald.fuji.core.command.argument.structure.CommandArgument;
import io.github.sakurawald.fuji.core.command.argument.wrapper.impl.ItemStackWrapper;
import io.github.sakurawald.fuji.core.command.processor.CommandAnnotationProcessor;
import lombok.SneakyThrows;
import net.minecraft.command.argument.ItemStackArgument;
import net.minecraft.command.argument.ItemStackArgumentType;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.ServerCommandSource;

import java.util.List;
import org.jetbrains.annotations.NotNull;

public class ItemStackArgumentTypeAdapter extends BaseArgumentTypeAdapter {

    @Override
    protected ArgumentType<?> makeArgumentType() {
        return ItemStackArgumentType.itemStack(CommandAnnotationProcessor.COMMAND_REGISTRY_ACCESS);
    }

    @SneakyThrows
    @Override
    public Object makeArgumentValue(@NotNull CommandContext<ServerCommandSource> context, @NotNull CommandArgument commandArgument) {
        ItemStackArgument itemStackArgument = ItemStackArgumentType.getItemStackArgument(context, commandArgument.getArgumentName());
        ItemStack itemStack = itemStackArgument.createStack(1, false);
        String inputString = itemStackArgument.asString(RegistryHelper.getDefaultWrapperLookup());
        return new ItemStackWrapper(itemStack, inputString);
    }

    @Override
    public List<Class<?>> getTypeClasses() {
        return List.of(ItemStackWrapper.class);
    }

    @Override
    public List<String> getTypeNames() {
        return List.of("item", "itemstack");
    }
}
