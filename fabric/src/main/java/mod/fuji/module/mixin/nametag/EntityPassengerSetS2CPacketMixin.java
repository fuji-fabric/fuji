package mod.fuji.module.mixin.nametag;

import mod.fuji.core.auxiliary.minecraft.PacketHelper;
import mod.fuji.module.initializer.nametag.service.NametagService;
import net.minecraft.world.entity.Entity;
import net.minecraft.network.protocol.game.ClientboundSetPassengersPacket;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientboundSetPassengersPacket.class)
public class EntityPassengerSetS2CPacketMixin {

    @Mutable
    @Shadow
    @Final
    public int[] passengers;

    @Inject(method = "<init>(Lnet/minecraft/world/entity/Entity;)V", at = @At("RETURN"))
    @SuppressWarnings("UnnecessaryLocalVariable")
    void appendNametagEntityAsPassenger(Entity entity, CallbackInfo ci) {
        if (entity instanceof ServerPlayer player) {
            NametagService
                .getNametagEntity(player)
                .ifPresent(nametagEntity -> {
                    int[] newValue = PacketHelper.makeAppendedArray(passengers, nametagEntity.getId());
                    passengers = newValue;
                });
        }
    }

}
