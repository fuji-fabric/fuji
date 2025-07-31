package io.github.sakurawald.fuji.core.structure;

import io.github.sakurawald.fuji.core.auxiliary.minecraft.RegistryHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import lombok.Data;
import lombok.With;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.s2c.play.PositionFlag;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import java.util.Set;
import java.util.EnumSet;

@Data
@With
public class GlobalPos {
    final String level;
    final double x;
    final double y;
    final double z;
    final float yaw;
    final float pitch;

    public GlobalPos(String level, double x, double y, double z, float yaw, float pitch) {
        this.level = level;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public GlobalPos(@NotNull World level, double x, double y, double z, float yaw, float pitch) {
        this(RegistryHelper.toString(level), x, y, z, yaw, pitch);
    }

    public static @NotNull GlobalPos of(@NotNull ServerPlayerEntity player) {
        return new GlobalPos(RegistryHelper.toString(player.getWorld()), player.getX(), player.getY(), player.getZ(), player.getYaw(), player.getPitch());
    }

    public static @NotNull GlobalPos of(@NotNull ServerCommandSource source) {
        Entity entity = source.getEntity();
        if (entity != null) {
            return new GlobalPos(RegistryHelper.toString(entity.getWorld()), entity.getX(), entity.getY(), entity.getZ(), entity.getYaw(), entity.getPitch());
        }

        return new GlobalPos(RegistryHelper.toString(source.getWorld()), source.getPosition().getX(), source.getPosition().getY(), source.getPosition().getZ(), source.getRotation().x, source.getRotation().y);
    }

    public boolean sameLevel(@NotNull World level) {
        return this.level.equals(RegistryHelper.toString(level));
    }

    @SuppressWarnings("unused")
    public double distanceToSqr(@NotNull GlobalPos globalPos) {
        if (!this.level.equals(globalPos.level)) return Double.MAX_VALUE;
        double x = this.x - globalPos.x;
        double y = this.y - globalPos.y;
        double z = this.z - globalPos.z;
        return x * x + y * y + z * z;
    }

    public void teleport(@NotNull ServerPlayerEntity player, Set<PositionFlag> flags) {
        /* Get the dimension instance from server. */
        ServerWorld dimension = RegistryHelper.getServerWorld(this.level);
        if (dimension == null) {
            TextHelper.sendTextByKey(player, "world.dimension.not_found", this.level);
            return;
        }

        /* Make position flags. */
        #if MC_VER <= MC_1_21
            player.teleport(dimension, this.x, this.y, this.z, flags, this.yaw, this.pitch);
        #elif MC_VER > MC_1_21
            player.teleport(dimension, this.x, this.y, this.z, flags, this.yaw, this.pitch, true);
        #endif
    }

    public void teleport(@NotNull ServerPlayerEntity player) {
        teleport(player, EnumSet.noneOf(PositionFlag.class));
    }
}
