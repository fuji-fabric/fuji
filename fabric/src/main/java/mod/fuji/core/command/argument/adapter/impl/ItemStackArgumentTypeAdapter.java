package mod.fuji.core.command.argument.adapter.impl;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import mod.fuji.core.auxiliary.minecraft.ItemStackHelper;
import mod.fuji.core.command.argument.adapter.abst.BaseArgumentTypeAdapter;
import mod.fuji.core.command.argument.structure.CommandArgument;
import mod.fuji.core.command.argument.wrapper.impl.ItemStackWrapper;
import mod.fuji.core.command.processor.CommandAnnotationProcessor;
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

    @SneakyThrows(CommandSyntaxException.class)
    @Override
    public Object makeArgumentValue(@NotNull CommandContext<ServerCommandSource> context, @NotNull CommandArgument commandArgument) {
        ItemStackArgument itemStackArgument = ItemStackArgumentType.getItemStackArgument(context, commandArgument.getArgumentName());
        ItemStack itemStack = ItemStackHelper.Parser.createItemStack(itemStackArgument);
        String inputString;

        #if MC_VER <= MC_1_20_4
        inputString = itemStackArgument.asString();
        #elif MC_VER > MC_1_20_4
        inputString = itemStackArgument.asString(mod.fuji.core.auxiliary.minecraft.RegistryHelper.getDefaultWrapperLookup());
        #endif

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
