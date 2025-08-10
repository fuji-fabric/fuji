package io.github.sakurawald.fuji.core.command.argument.adapter.impl;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import io.github.sakurawald.fuji.core.command.argument.adapter.abst.BaseArgumentTypeAdapter;
import io.github.sakurawald.fuji.core.command.argument.structure.Argument;
#if MC_VER <= MC_1_20_2
import io.github.sakurawald.fuji.core.command.argument.wrapper.impl.GreedyString;
import com.mojang.brigadier.arguments.StringArgumentType;
#elif MC_VER > MC_1_20_2 && MC_VER <= MC_1_20_4
import net.minecraft.command.argument.StyleArgumentType;
#elif MC_VER > MC_1_20_4
import net.minecraft.command.argument.StyleArgumentType;
#endif

#if MC_VER <= MC_1_20_2
#elif MC_VER > MC_1_20_2
import io.github.sakurawald.fuji.core.command.processor.CommandAnnotationProcessor;
#endif

import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Style;

import java.util.List;
import org.jetbrains.annotations.NotNull;

public class StyleArgumentTypeAdapter extends BaseArgumentTypeAdapter {
    @Override
    protected ArgumentType<?> makeArgumentType() {
        #if MC_VER <= MC_1_20_2
        return StringArgumentType.greedyString();
        #elif MC_VER > MC_1_20_2 && MC_VER <= MC_1_20_4
        return StyleArgumentType.style();
        #elif MC_VER > MC_1_20_4
        return StyleArgumentType.style(CommandAnnotationProcessor.COMMAND_REGISTRY_ACCESS);
        #endif
    }

    @Override
    protected Object makeArgumentObject(@NotNull CommandContext<ServerCommandSource> context, @NotNull Argument argument) {
        #if MC_VER <= MC_1_20_2
        return new GreedyString(StringArgumentType.getString(context, argument.getArgumentName()));
        #elif MC_VER > MC_1_20_2
        return StyleArgumentType.getStyle(context, argument.getArgumentName());
        #endif
    }

    @Override
    public List<Class<?>> getTypeClasses() {
        return List.of(Style.class);
    }

    @Override
    public List<String> getTypeStrings() {
        return List.of("style");
    }
}
