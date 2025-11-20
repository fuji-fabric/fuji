package mod.fuji.core.command.argument.adapter.impl;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import mod.fuji.core.command.argument.adapter.abst.BaseArgumentTypeAdapter;
import mod.fuji.core.command.argument.structure.CommandArgument;

#if MC_VER <= MC_1_20_2
import mod.fuji.core.command.argument.wrapper.impl.GreedyString;
import com.mojang.brigadier.arguments.StringArgumentType;
#elif MC_VER > MC_1_20_2
import net.minecraft.commands.arguments.StyleArgument;
#endif

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Style;

import java.util.List;
import org.jetbrains.annotations.NotNull;

public class StyleArgumentTypeAdapter extends BaseArgumentTypeAdapter {
    @Override
    protected ArgumentType<?> makeArgumentType() {
        #if MC_VER <= MC_1_20_2
        return StringArgumentType.greedyString();
        #elif MC_VER > MC_1_20_2 && MC_VER <= MC_1_20_4
        return StyleArgument.style();
        #elif MC_VER > MC_1_20_4
        return StyleArgument.style(mod.fuji.core.command.processor.CommandAnnotationProcessor.COMMAND_REGISTRY_ACCESS);
        #endif
    }

    @Override
    protected Object makeArgumentValue(@NotNull CommandContext<CommandSourceStack> context, @NotNull CommandArgument commandArgument) {
        #if MC_VER <= MC_1_20_2
        return new GreedyString(StringArgumentType.getString(context, commandArgument.getArgumentName()));
        #elif MC_VER > MC_1_20_2
        return StyleArgument.getStyle(context, commandArgument.getArgumentName());
        #endif
    }

    @Override
    public List<Class<?>> getTypeClasses() {
        return List.of(Style.class);
    }

    @Override
    public List<String> getTypeNames() {
        return List.of("style");
    }
}
