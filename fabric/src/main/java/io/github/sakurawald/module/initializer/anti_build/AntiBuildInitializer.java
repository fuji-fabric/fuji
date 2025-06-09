package io.github.sakurawald.module.initializer.anti_build;

import io.github.sakurawald.core.auxiliary.minecraft.PermissionHelper;
import io.github.sakurawald.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.core.config.handler.abst.BaseConfigurationHandler;
import io.github.sakurawald.core.config.handler.impl.ObjectConfigurationHandler;
import io.github.sakurawald.module.initializer.ModuleInitializer;
import io.github.sakurawald.module.initializer.anti_build.config.model.AntiBuildConfigModel;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

public class AntiBuildInitializer extends ModuleInitializer {
    public static final BaseConfigurationHandler<AntiBuildConfigModel> config = new ObjectConfigurationHandler<>(BaseConfigurationHandler.CONFIG_JSON, AntiBuildConfigModel.class);

    public static <T> void checkAntiBuild(PlayerEntity player, String antiType, Set<String> ids, String id, CallbackInfoReturnable<T> cir, T cancelWithValue, Supplier<Boolean> shouldSendFeedback) {
        if ((ids.contains(id) || ids.contains("*")) && !isAllowedByPlayerPermission(player, antiType, id)) {
            if (shouldSendFeedback.get()) {
                TextHelper.sendMessageByKey(player, "anti_build.disallow");
            }

            cir.setReturnValue(cancelWithValue);
        }
    }

    private static boolean isAllowedByPlayerPermission(PlayerEntity player, String antiType, String id) {
        return Optional.ofNullable(player)
            .map(p -> PermissionHelper.hasPermission(player.getUuid(), "fuji.anti_build.%s.bypass.%s".formatted(antiType, id)))
            .orElse(true);
    }
}
