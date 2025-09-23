package mod.fuji.core.command.argument.adapter.impl;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import mod.fuji.core.auxiliary.minecraft.CommandHelper;
import mod.fuji.core.command.argument.adapter.abst.BaseArgumentTypeAdapter;
import mod.fuji.core.command.argument.structure.CommandArgument;
import mod.fuji.core.command.processor.CommandAnnotationProcessor;
import net.minecraft.command.argument.ParticleEffectArgumentType;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.server.command.ServerCommandSource;

import java.util.List;
import org.jetbrains.annotations.NotNull;

public class ParticleEffectArgumentTypeAdapter extends BaseArgumentTypeAdapter {
    @Override
    protected ArgumentType<?> makeArgumentType() {
        return ParticleEffectArgumentType.particleEffect(CommandHelper.getCommandRegistryAccess());
    }

    @Override
    protected Object makeArgumentValue(@NotNull CommandContext<ServerCommandSource> context, @NotNull CommandArgument commandArgument) {
        return ParticleEffectArgumentType.getParticle(context, commandArgument.getArgumentName());
    }

    @Override
    public List<Class<?>> getTypeClasses() {
        return List.of(ParticleEffect.class);
    }

    @Override
    public List<String> getTypeNames() {
        return List.of("particle-effect");
    }
}
