package io.github.sakurawald.fuji.core.command.argument.adapter.impl;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import io.github.sakurawald.fuji.core.command.argument.adapter.abst.BaseArgumentTypeAdapter;
import io.github.sakurawald.fuji.core.command.argument.structure.Argument;
#if MC_VER <= MC_1_20_4
#elif MC_VER > MC_1_20_4
import io.github.sakurawald.fuji.core.command.processor.CommandAnnotationProcessor;
#endif
import net.minecraft.command.argument.TextArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import java.util.List;
import org.jetbrains.annotations.NotNull;

public class TextArgumentTypeAdapter extends BaseArgumentTypeAdapter {
    @Override
    protected ArgumentType<?> makeArgumentType() {
        #if MC_VER <= MC_1_20_4
        return TextArgumentType.text();
        #elif MC_VER > MC_1_20_4
        return TextArgumentType.text(CommandAnnotationProcessor.COMMAND_REGISTRY_ACCESS);
        #endif
    }

    @Override
    protected Object makeArgumentValue(@NotNull CommandContext<ServerCommandSource> context, @NotNull Argument argument) {
        return TextArgumentType.getTextArgument(context, argument.getArgumentName());
    }

    @Override
    public List<Class<?>> getTypeClasses() {
        return List.of(Text.class);
    }

    @Override
    public List<String> getTypeStrings() {
        return List.of("text");
    }
}
