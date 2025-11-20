package mod.fuji.core.command.argument.adapter.impl;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import mod.fuji.core.auxiliary.minecraft.CommandHelper;import mod.fuji.core.command.argument.adapter.abst.BaseArgumentTypeAdapter;
import mod.fuji.core.command.argument.structure.CommandArgument;
import mod.fuji.core.command.argument.wrapper.impl.NotSupportedType;
import lombok.SneakyThrows;
#if MC_VER <= MC_1_20_4
import com.mojang.brigadier.arguments.StringArgumentType;
import mod.fuji.core.command.argument.wrapper.impl.GreedyString;
#elif MC_VER > MC_1_20_4
import mod.fuji.core.command.processor.CommandAnnotationProcessor;
import net.minecraft.commands.arguments.ResourceArgument;
import net.minecraft.core.registries.Registries;
#endif
import net.minecraft.commands.CommandSourceStack;

import java.util.List;
import org.jetbrains.annotations.NotNull;

public class EntityTypeArgumentAdapter extends BaseArgumentTypeAdapter {
    @Override
    protected ArgumentType<?> makeArgumentType() {
        #if MC_VER <= MC_1_20_4
            return StringArgumentType.greedyString();
        #elif MC_VER > MC_1_20_4
            return ResourceArgument.resource(CommandHelper.getCommandRegistryAccess(), Registries.ENTITY_TYPE);
        #endif
    }

    @SneakyThrows(Throwable.class)
    @Override
    protected Object makeArgumentValue(@NotNull CommandContext<CommandSourceStack> context, @NotNull CommandArgument commandArgument) {
        #if MC_VER <= MC_1_20_4
        return new GreedyString(StringArgumentType.getString(context, commandArgument.getArgumentName()));
        #elif MC_VER > MC_1_20_4
        return ResourceArgument.getResource(context, commandArgument.getArgumentName(), Registries.ENTITY_TYPE);
        #endif
    }

    @Override
    public List<Class<?>> getTypeClasses() {
        return List.of(NotSupportedType.class);
    }

    @Override
    public List<String> getTypeNames() {
        return List.of("entity-type");
    }

    @Override
    public boolean isVanillaMinecraftArgumentType() {
        return true;
    }
}
