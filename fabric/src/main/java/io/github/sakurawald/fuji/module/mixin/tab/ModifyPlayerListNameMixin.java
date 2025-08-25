package io.github.sakurawald.fuji.module.mixin.tab;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import io.github.sakurawald.fuji.core.auxiliary.RandomUtil;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.fuji.module.initializer.tab.TabListInitializer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = ServerPlayerEntity.class)
public abstract class ModifyPlayerListNameMixin {

    @Unique
    private final ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;

    @ModifyReturnValue(method = "getPlayerListName", at = @At("RETURN"))
    @NotNull
    Text modifyPlayerListName(@Nullable Text original) {
        // Respect other's modification.
        if (original == null) {
            return TextHelper.getTextByValue(player, RandomUtil.drawList(TabListInitializer.config.model().getStyle().getBody()));
        }

        return original;
    }
}
