package mod.fuji.core.command.argument.adapter.impl;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import mod.fuji.core.command.argument.adapter.abst.BaseArgumentTypeAdapter;
import mod.fuji.core.command.argument.structure.CommandArgument;
import net.minecraft.commands.arguments.ComponentArgument;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;

import java.util.List;
import org.jetbrains.annotations.NotNull;

public class TextArgumentTypeAdapter extends BaseArgumentTypeAdapter {
    @Override
    protected ArgumentType<?> makeArgumentType() {
        #if MC_VER <= MC_1_20_4
        return ComponentArgument.textComponent();
        #elif MC_VER > MC_1_20_4
        return ComponentArgument.textComponent(getCommandRegistryAccess());
        #endif
    }

    @Override
    protected Object makeArgumentValue(@NotNull CommandContext<CommandSourceStack> context, @NotNull CommandArgument commandArgument) {
        #if MC_VER <= MC_1_21_4
        return ComponentArgument.getComponent(context, commandArgument.getArgumentName());
        #elif MC_VER > MC_1_21_4
        return ComponentArgument.getRawComponent(context, commandArgument.getArgumentName());
        #endif
    }

    @Override
    public List<Class<?>> getTypeClasses() {
        return List.of(Component.class);
    }

    @Override
    public List<String> getTypeNames() {
        return List.of("text");
    }
}
