package mod.fuji.core.command.argument.adapter.impl;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import mod.fuji.core.command.argument.adapter.abst.BaseArgumentTypeAdapter;
import mod.fuji.core.command.argument.structure.CommandArgument;
#if MC_VER <= MC_1_20_1
import com.mojang.brigadier.arguments.StringArgumentType;
import mod.fuji.core.command.argument.wrapper.impl.GreedyString;
import mod.fuji.core.command.argument.wrapper.impl.NotSupportedType;
#elif MC_VER > MC_1_20_1
import net.minecraft.commands.arguments.ScoreboardSlotArgument;
import net.minecraft.world.scores.DisplaySlot;
#endif

import net.minecraft.commands.CommandSourceStack;

import java.util.List;
import org.jetbrains.annotations.NotNull;

public class ScoreboardSlotArgumentTypeAdapter extends BaseArgumentTypeAdapter {
    @Override
    protected ArgumentType<?> makeArgumentType() {
        #if MC_VER <= MC_1_20_1
        return StringArgumentType.greedyString();
        #elif MC_VER > MC_1_20_1
        return ScoreboardSlotArgument.displaySlot();
        #endif

    }

    @Override
    protected Object makeArgumentValue(@NotNull CommandContext<CommandSourceStack> context, @NotNull CommandArgument commandArgument) {
        #if MC_VER <= MC_1_20_1
        return new GreedyString(StringArgumentType.getString(context, commandArgument.getArgumentName()));
        #elif MC_VER > MC_1_20_1
        return ScoreboardSlotArgument.getDisplaySlot(context, commandArgument.getArgumentName());
        #endif
    }

    @Override
    public List<Class<?>> getTypeClasses() {
        #if MC_VER <= MC_1_20_1
        return List.of(NotSupportedType.class);
        #elif MC_VER > MC_1_20_1
        return List.of(DisplaySlot.class);
        #endif
    }

    @Override
    public List<String> getTypeNames() {
        return List.of("scoreboard-slot");
    }
}
