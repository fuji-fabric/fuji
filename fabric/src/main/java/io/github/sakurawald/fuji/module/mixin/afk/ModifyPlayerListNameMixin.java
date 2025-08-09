package io.github.sakurawald.fuji.module.mixin.afk;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import io.github.sakurawald.fuji.core.document.annotation.TestCase;
import io.github.sakurawald.fuji.module.initializer.afk.AfkInitializer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

// NOTE: To override tab list name in `tab list module`
@Mixin(value = ServerPlayerEntity.class, priority = 1000 + 250)
public class ModifyPlayerListNameMixin {

    @Unique
    private final ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;

    @TestCase(action = "Issue `/afk` and see the player list.", targets = "The display name of an afk player should be modified.")
    @ModifyReturnValue(method = "getPlayerListName", at = @At("RETURN"))
    public Text handlePlayerListName(Text original) {
        if (AfkInitializer.isAfk(player)) {
            return AfkInitializer.getAfkText(player);
        }

        return original;
    }


}
