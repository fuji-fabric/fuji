package io.github.sakurawald.fuji.module.initializer.multiplier;

import io.github.sakurawald.fuji.core.auxiliary.minecraft.PermissionHelper;
import io.github.sakurawald.fuji.core.document.descriptor.MetaDescriptor;
import io.github.sakurawald.fuji.module.initializer.ModuleInitializer;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class MultiplierInitializer extends ModuleInitializer{

    public static final MetaDescriptor<Float> MULTIPLIER_META = new MetaDescriptor<>("fuji.multiplier.<multiplier-type>.<id>", Float::valueOf, """
        Specify the `multiply factor` for a specified `type` for this player.
        """);

    public static float transform(@NotNull ServerPlayerEntity player, String type, String key, float f) {
        Optional<Float> meta = PermissionHelper.getMeta(player.getUuid(), MULTIPLIER_META, type, key);
        return meta.map(factor -> f * factor).orElse(f);
    }
}
