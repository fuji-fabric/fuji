package mod.fuji.module.mixin.core.server;

import mod.fuji.core.auxiliary.LogUtil;
import mod.fuji.core.auxiliary.minecraft.RegistryHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Mixin(ServerLevel.class)
public class ServerWorldMixin {

    @Mutable
    @Final
    @Shadow
    List<ServerPlayer> players;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void patchCopyOnWriteArrayListForPlayersInServerWorld(CallbackInfo ci) {
        ServerLevel thiz = (ServerLevel) (Object) this;
        players = new CopyOnWriteArrayList<>() {
            {
                LogUtil.debug("Patch CopyOnWriteArrayList for `players` field in ServerWorld {}", RegistryHelper.getIdAsString(thiz));
            }
        };
    }
}
