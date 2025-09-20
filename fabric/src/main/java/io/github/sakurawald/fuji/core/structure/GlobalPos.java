package io.github.sakurawald.fuji.core.structure;

import io.github.sakurawald.fuji.core.auxiliary.minecraft.EntityHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.PlayerHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.RegistryHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.WorldHelper;
import java.util.List;
import java.util.Optional;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.s2c.play.PositionFlag;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import java.util.Set;
import java.util.EnumSet;

@Data
@NoArgsConstructor
@With
public class GlobalPos {
    String level;
    double x;
    double y;
    double z;
    float yaw;
    float pitch;

    public GlobalPos(String level, double x, double y, double z, float yaw, float pitch) {
        this.level = level;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public GlobalPos(@NotNull World level, double x, double y, double z, float yaw, float pitch) {
        this(RegistryHelper.getIdAsString(level), x, y, z, yaw, pitch);
    }

    public List<Text> asLore(@NotNull ServerPlayerEntity player) {
        return List.of(
            TextHelper.getTextByKey(player, "dimension", this.level),
            TextHelper.getTextByKey(player, "x", this.x),
            TextHelper.getTextByKey(player, "y", this.y),
            TextHelper.getTextByKey(player, "z", this.z),
            TextHelper.getTextByKey(player, "yaw", this.yaw),
            TextHelper.getTextByKey(player, "pitch", this.pitch)
        );
    }

    public static @NotNull GlobalPos of(@NotNull ServerPlayerEntity player) {
        ServerWorld serverWorld = PlayerHelper.getServerWorld(player);
        return new GlobalPos(RegistryHelper.getIdAsString(serverWorld), player.getX(), player.getY(), player.getZ(), player.getYaw(), player.getPitch());
    }

    public static @NotNull GlobalPos of(@NotNull ServerCommandSource source) {
        Entity entity = source.getEntity();
        if (entity != null) {
            ServerWorld serverWorld = EntityHelper.getServerWorld(entity);
            return new GlobalPos(RegistryHelper.getIdAsString(serverWorld), entity.getX(), entity.getY(), entity.getZ(), entity.getYaw(), entity.getPitch());
        }

        return new GlobalPos(RegistryHelper.getIdAsString(source.getWorld()), source.getPosition().getX(), source.getPosition().getY(), source.getPosition().getZ(), source.getRotation().x, source.getRotation().y);
    }

    public boolean sameLevel(@NotNull World level) {
        return this.level.equals(RegistryHelper.getIdAsString(level));
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
        Optional<ServerWorld> dimension = WorldHelper.getWorld(this.level);
        dimension.ifPresentOrElse($dimension -> {
            /* Make position flags. */
            #if MC_VER <= MC_1_21
            player.teleport($dimension, this.x, this.y, this.z, flags, this.yaw, this.pitch);
            #elif MC_VER > MC_1_21
            player.teleport($dimension, this.x, this.y, this.z, flags, this.yaw, this.pitch, true);
            #endif
        }, () -> TextHelper.sendTextByKey(player, "world.dimension.not_found", this.level));
    }

    public void teleport(@NotNull ServerPlayerEntity player) {
        teleport(player, EnumSet.noneOf(PositionFlag.class));
    }
}
