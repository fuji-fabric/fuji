package mod.fuji.module.mixin.nametag;

import mod.fuji.core.auxiliary.minecraft.PacketHelper;
import mod.fuji.module.initializer.nametag.service.NametagService;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.s2c.play.EntityPassengersSetS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityPassengersSetS2CPacket.class)
public class EntityPassengerSetS2CPacketMixin {

    @Mutable
    @Shadow
    @Final
    public int[] passengerIds;

    @Inject(method = "<init>(Lnet/minecraft/entity/Entity;)V", at = @At("RETURN"))
    @SuppressWarnings("UnnecessaryLocalVariable")
    void appendNametagEntityAsPassenger(Entity entity, CallbackInfo ci) {
        if (entity instanceof ServerPlayerEntity player) {
            NametagService
                .getNametagEntity(player)
                .ifPresent(nametagEntity -> {
                    int[] newValue = PacketHelper.makeAppendedArray(passengerIds, nametagEntity.getId());
                    passengerIds = newValue;
                });
        }
    }

}
