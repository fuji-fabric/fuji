package io.github.sakurawald.core.command.argument.adapter.impl;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import io.github.sakurawald.core.command.argument.adapter.abst.BaseArgumentTypeAdapter;
import io.github.sakurawald.core.command.argument.exception.ArgumentTypeNotSupportedException;
import io.github.sakurawald.core.command.argument.structure.Argument;
import io.github.sakurawald.core.command.argument.wrapper.impl.NotSupportedType;
#if MC_VER <= MC_1_20_4
#elif MC_VER > MC_1_20_4
import net.minecraft.command.argument.SlotRangeArgumentType;
import net.minecraft.inventory.SlotRange;
#endif
import net.minecraft.server.command.ServerCommandSource;

import java.util.List;

public class SlotRangeArgumentTypeAdapter extends BaseArgumentTypeAdapter {
    @Override
    protected ArgumentType<?> makeArgumentType() {
        #if MC_VER <= MC_1_20_4
            throw new ArgumentTypeNotSupportedException();
        #elif MC_VER > MC_1_20_4
            return SlotRangeArgumentType.slotRange();
        #endif
    }

    @Override
    protected Object makeArgumentObject(CommandContext<ServerCommandSource> context, Argument argument) {
        #if MC_VER <= MC_1_20_4
            throw new ArgumentTypeNotSupportedException();
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
