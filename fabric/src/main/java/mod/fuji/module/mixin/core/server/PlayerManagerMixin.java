package mod.fuji.module.mixin.core.server;

import mod.fuji.core.auxiliary.LogUtil;
import net.minecraft.server.players.PlayerList;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Mixin(PlayerList.class)
public class PlayerManagerMixin {

    @Mutable
    @Final
    @Shadow
    private List<ServerPlayer> players;

    @Inject(method = "<init>", at = @At("RETURN"))
    void patchCopyOnWriteArrayListForServerPlayerList(CallbackInfo ci) {
        players = new CopyOnWriteArrayList<>() {
            {
                LogUtil.debug("Patch CopyOnWriteArrayList for `players` field in PlayerManager");
            }
        };
    }
}
