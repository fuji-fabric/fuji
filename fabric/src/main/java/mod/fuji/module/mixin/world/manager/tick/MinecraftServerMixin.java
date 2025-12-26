package mod.fuji.module.mixin.world.manager.tick;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import mod.fuji.module.initializer.world.manager.structure.util.SnapshotArrayList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin {

    @ModifyReturnValue(method = "getAllLevels", at = @At("RETURN"))
    Iterable<ServerLevel> copyBeforeTicking(Iterable<ServerLevel> original) {
        // NOTE: After issue /world reset, it's possible that all the worlds will be ticked twice.
        return new SnapshotArrayList<>(original);
    }

}
