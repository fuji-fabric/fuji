package io.github.sakurawald.fuji.core.command.argument.adapter.impl;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import io.github.sakurawald.fuji.core.command.argument.adapter.abst.BaseArgumentTypeAdapter;
import io.github.sakurawald.fuji.core.command.argument.structure.Argument;
#if MC_VER <= MC_1_20_4
import com.mojang.brigadier.arguments.StringArgumentType;
import io.github.sakurawald.fuji.core.command.argument.wrapper.impl.GreedyString;
#elif MC_VER > MC_1_20_4
import net.minecraft.command.argument.SlotRangeArgumentType;
import net.minecraft.inventory.SlotRange;
#endif
import net.minecraft.server.command.ServerCommandSource;

import java.util.List;
import org.jetbrains.annotations.NotNull;

public class SlotRangeArgumentTypeAdapter extends BaseArgumentTypeAdapter {
    @Override
    protected ArgumentType<?> makeArgumentType() {
        #if MC_VER <= MC_1_20_4
            return StringArgumentType.greedyString();
        #elif MC_VER > MC_1_20_4
            return SlotRangeArgumentType.slotRange();
        #endif
    }

    @Override
    protected Object makeArgumentObject(@NotNull CommandContext<ServerCommandSource> context, @NotNull Argument argument) {
        #if MC_VER <= MC_1_20_4
        return new GreedyString(StringArgumentType.getString(context, argument.getArgumentName()));
        #elif MC_VER > MC_1_20_4
        return SlotRangeArgumentType.getSlotRange(context, argument.getArgumentName());
        #endif
    }

    @Override
    public List<Class<?>> getTypeClasses() {
        #if MC_VER <= MC_1_20_4
            return List.of(NotSupportedType.class);
        #elif MC_VER > MC_1_20_4
            return List.of(SlotRange.class);
        #endif
    }

    @Override
    public List<String> getTypeStrings() {
        return List.of("slot-range");
    }
}
