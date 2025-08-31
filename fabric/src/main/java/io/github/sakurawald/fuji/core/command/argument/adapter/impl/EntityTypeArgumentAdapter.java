package io.github.sakurawald.fuji.core.command.argument.adapter.impl;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.sakurawald.fuji.core.command.argument.adapter.abst.BaseArgumentTypeAdapter;
import io.github.sakurawald.fuji.core.command.argument.structure.CommandArgument;
import io.github.sakurawald.fuji.core.command.argument.wrapper.impl.NotSupportedType;
import lombok.SneakyThrows;
#if MC_VER <= MC_1_20_4
import com.mojang.brigadier.arguments.StringArgumentType;
import io.github.sakurawald.fuji.core.command.argument.wrapper.impl.GreedyString;
#elif MC_VER > MC_1_20_4
import io.github.sakurawald.fuji.core.command.processor.CommandAnnotationProcessor;
import net.minecraft.command.argument.RegistryEntryReferenceArgumentType;
import net.minecraft.registry.RegistryKeys;
#endif
import net.minecraft.server.command.ServerCommandSource;

import java.util.List;
import org.jetbrains.annotations.NotNull;

public class EntityTypeArgumentAdapter extends BaseArgumentTypeAdapter {
    @Override
    protected ArgumentType<?> makeArgumentType() {
        #if MC_VER <= MC_1_20_4
            return StringArgumentType.greedyString();
        #elif MC_VER > MC_1_20_4
            return RegistryEntryReferenceArgumentType.registryEntry(CommandAnnotationProcessor.COMMAND_REGISTRY_ACCESS, RegistryKeys.ENTITY_TYPE);
        #endif
    }

    @SneakyThrows(CommandSyntaxException.class)
    @Override
    protected Object makeArgumentValue(@NotNull CommandContext<ServerCommandSource> context, @NotNull CommandArgument commandArgument) {
        #if MC_VER <= MC_1_20_4
        return new GreedyString(StringArgumentType.getString(context, commandArgument.getArgumentName()));
        #elif MC_VER > MC_1_20_4
        return RegistryEntryReferenceArgumentType.getRegistryEntry(context, commandArgument.getArgumentName(), RegistryKeys.ENTITY_TYPE);
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
