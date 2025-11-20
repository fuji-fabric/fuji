package mod.fuji.core.structure;

import mod.fuji.core.auxiliary.minecraft.EntityHelper;
import mod.fuji.core.auxiliary.minecraft.PlayerHelper;
import mod.fuji.core.auxiliary.minecraft.RegistryHelper;
import mod.fuji.core.auxiliary.minecraft.TextHelper;
import mod.fuji.core.auxiliary.minecraft.WorldHelper;
import java.util.List;
import java.util.Optional;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Relative;
import net.minecraft.resources.ResourceKey;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import java.util.Set;
import java.util.EnumSet;

@Data
@NoArgsConstructor
@With
public class GlobalPos {
    @NotNull String level;
    double x;
    double y;
    double z;
    float yaw;
    float pitch;

    public GlobalPos(@NotNull String world, double x, double y, double z, float yaw, float pitch) {
        this.level = world;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public GlobalPos(@NotNull Level world, double x, double y, double z, float yaw, float pitch) {
        this(RegistryHelper.getIdAsString(world), x, y, z, yaw, pitch);
    }

    public List<Component> asLore(@NotNull ServerPlayer player) {
        return List.of(
            TextHelper.getTextByKey(player, "dimension", this.level),
            TextHelper.getTextByKey(player, "x", this.x),
            TextHelper.getTextByKey(player, "y", this.y),
            TextHelper.getTextByKey(player, "z", this.z),
            TextHelper.getTextByKey(player, "yaw", this.yaw),
            TextHelper.getTextByKey(player, "pitch", this.pitch)
        );
    }

    public static @NotNull GlobalPos of(@NotNull Level world, @NotNull BlockPos blockPos) {
        return new GlobalPos(RegistryHelper.getIdAsString(world), blockPos.getX(), blockPos.getY(), blockPos.getZ(), 0, 0);
    }

    public static @NotNull GlobalPos of(@NotNull ResourceKey<Level> world, @NotNull BlockPos blockPos) {
        return new GlobalPos(RegistryHelper.getIdAsString(world), blockPos.getX(), blockPos.getY(), blockPos.getZ(), 0, 0);
    }

    public static @NotNull GlobalPos of(@NotNull ServerPlayer player) {
        ServerLevel serverWorld = PlayerHelper.getServerWorld(player);
        return new GlobalPos(RegistryHelper.getIdAsString(serverWorld), player.getX(), player.getY(), player.getZ(), player.getYRot(), player.getXRot());
    }

    public static @NotNull GlobalPos of(@NotNull CommandSourceStack source) {
        Entity entity = source.getEntity();
        if (entity != null) {
            ServerLevel serverWorld = EntityHelper.getServerWorld(entity);
            return new GlobalPos(RegistryHelper.getIdAsString(serverWorld), entity.getX(), entity.getY(), entity.getZ(), entity.getYRot(), entity.getXRot());
        }

        return new GlobalPos(RegistryHelper.getIdAsString(source.getLevel()), source.getPosition().x(), source.getPosition().y(), source.getPosition().z(), source.getRotation().x, source.getRotation().y);
    }

    public @NotNull BlockPos toBlockPos() {
        return new BlockPos((int) this.x, (int) this.y, (int) this.z);
    }

    public boolean sameLevel(@NotNull Level level) {
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

    public void teleport(@NotNull ServerPlayer player, Set<Relative> flags) {
        /* Get the dimension instance from server. */
        Optional<ServerLevel> dimension = WorldHelper.getWorld(this.level);
        dimension.ifPresentOrElse($dimension -> {
            /* Make position flags. */
            #if MC_VER <= MC_1_21
            player.teleport($dimension, this.x, this.y, this.z, flags, this.yaw, this.pitch);
            #elif MC_VER > MC_1_21
            player.teleportTo($dimension, this.x, this.y, this.z, flags, this.yaw, this.pitch, true);
            #endif
        }, () -> TextHelper.sendTextByKey(player, "world.dimension.not_found", this.level));
    }

    public void teleport(@NotNull ServerPlayer player) {
        teleport(player, EnumSet.noneOf(Relative.class));
    }
}
