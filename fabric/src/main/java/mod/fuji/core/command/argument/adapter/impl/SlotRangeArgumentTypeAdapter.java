package mod.fuji.core.command.argument.adapter.impl;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import mod.fuji.core.command.argument.adapter.abst.BaseArgumentTypeAdapter;
import mod.fuji.core.command.argument.structure.CommandArgument;
#if MC_VER <= MC_1_20_4
import com.mojang.brigadier.arguments.StringArgumentType;
import mod.fuji.core.command.argument.wrapper.impl.GreedyString;
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
    protected Object makeArgumentValue(@NotNull CommandContext<ServerCommandSource> context, @NotNull CommandArgument commandArgument) {
        #if MC_VER <= MC_1_20_4
        return new GreedyString(StringArgumentType.getString(context, commandArgument.getArgumentName()));
        #elif MC_VER > MC_1_20_4
        return SlotRangeArgumentType.getSlotRange(context, commandArgument.getArgumentName());
        #endif
    }

    @Override
    public List<Class<?>> getTypeClasses() {
        #if MC_VER <= MC_1_20_4
            return List.of(mod.fuji.core.command.argument.wrapper.impl.NotSupportedType.class);
        #elif MC_VER > MC_1_20_4
            return List.of(SlotRange.class);
        #endif
    }

    @Override
    public List<String> getTypeNames() {
        return List.of("slot-range");
    }
}
