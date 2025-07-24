package io.github.sakurawald.fuji.module.initializer.skin.service;

import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import io.github.sakurawald.fuji.core.auxiliary.LogUtil;
import io.github.sakurawald.fuji.core.auxiliary.RandomUtil;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.EntityHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.PlayerHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.ServerHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.fuji.core.config.handler.abst.BaseConfigurationHandler;
import io.github.sakurawald.fuji.module.initializer.skin.SkinInitializer;
import io.github.sakurawald.fuji.module.initializer.skin.structure.SkinDescriptor;
import io.github.sakurawald.fuji.module.initializer.skin.structure.SkinStorage;
import it.unimi.dsi.fastutil.Pair;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.network.packet.s2c.play.DifficultyS2CPacket;
import net.minecraft.network.packet.s2c.play.EntitiesDestroyS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityPassengersSetS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityPositionS2CPacket;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityStatusEffectS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityTrackerUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ExperienceBarUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerRemoveS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerRespawnS2CPacket;
import net.minecraft.network.packet.s2c.play.UpdateSelectedSlotS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

#if MC_VER <= MC_1_20_1
import net.minecraft.world.biome.source.BiomeAccess;
#endif

#if MC_VER > MC_1_21
import net.minecraft.entity.player.PlayerPosition;
import java.util.Set;
#endif

public class SkinService {

    public static @NotNull Property getEffectiveSkinProperty(GameProfile gameProfile) {
        return SkinStorage
            .readSkinData(gameProfile);
    }

    public static int changeSkin(@NotNull ServerPlayerEntity player, @NotNull Supplier<Property> skinSupplier) {
        changeSkinAsync(player.getGameProfile(), skinSupplier)
            .thenAccept(success -> {

            if (!success) {
                TextHelper.sendTextByKey(player, "skin.action.failed");
                return;
            }

            TextHelper.sendTextByKey(player, "skin.action.ok");
        });

        return CommandHelper.Return.SUCCESS;
    }

    public static void modifyGameProfile(@NotNull GameProfile gameProfile, @NotNull Property skin) {
        LogUtil.debug("Modify the skin property for player {}. (skin = {})", gameProfile.getName(), skin);
        gameProfile.getProperties().removeAll("textures");
        gameProfile.getProperties().put("textures", skin);
    }

    private static @NotNull List<SkinDescriptor> getDefaultSkinList() {
        return SkinInitializer.config.model().getDefaultSkin().getDefaultSkinList();
    }

    public static @NotNull Property getDefaultSkin() {
        /* Get the preferred default skin. */
        String preferredDefaultSkinNameForNewPlayers = SkinInitializer.config.model().getDefaultSkin().getPreferredSkinName();
        Optional<SkinDescriptor> preferredDefaultSkin = getDefaultSkinList()
            .stream()
            .filter(it -> it.getSkinName().contains(preferredDefaultSkinNameForNewPlayers))
            .findFirst();
        if (preferredDefaultSkin.isPresent()) {
            return preferredDefaultSkin.get().getSkinProperty();
        }

        /* Get a random default skin. */
        return RandomUtil
            .drawList(getDefaultSkinList())
            .getSkinProperty();
    }

    private static boolean isSkinPropertyEqual(@NotNull Property x, @NotNull GameProfile y) {
        try {
            /* Make the x json object. */
            JsonObject xJsonObject = makeComparableJsonObjectFromSkinProperty(x);

            /* Make the y json object. */
            Optional<Property> py = y.getProperties()
                .get("textures")
                .stream()
                .findFirst();
            if (py.isEmpty()) {
                return false;
            }
            JsonObject yJsonObject = makeComparableJsonObjectFromSkinProperty(py.get());

            /* Compare x and y. */
            return xJsonObject.equals(yJsonObject);
        } catch (Exception e) {
            LogUtil.error("Failed to compare the skin textures: x = {}, y = {}", x, y, e);
            return false;
        }
    }

    private static @NotNull JsonObject makeComparableJsonObjectFromSkinProperty(Property property) {
        String jsonString = new String(Base64.getDecoder().decode(PlayerHelper.getPropertyValue(property)), StandardCharsets.UTF_8);
        JsonObject jsonObject = BaseConfigurationHandler.getGson().fromJson(jsonString, JsonObject.class);
        jsonObject.remove("timestamp");
        return jsonObject;
    }

    @SuppressWarnings("RedundantTypeArguments")
    private static @NotNull CompletableFuture<Boolean> changeSkinAsync(@NotNull GameProfile target, @NotNull Supplier<Property> skinSupplier) {
        MinecraftServer server = ServerHelper.getServer();

        return CompletableFuture
            .<Pair<GameProfile, Property>>supplyAsync(() -> {
            /* Resolve the skin property from the supplier. */
            @Nullable Property skinProperty = skinSupplier.get();
            LogUtil.debug("Resolved skin property from skin supplier: target = {}, skin = {}", target.getName(), skinProperty);
            if (skinProperty == null) {
                throw new IllegalStateException("Failed to resolve skin property from skin supplier.");
            }

            /* Update the skin data. */
            SkinStorage.writeSkinData(target.getId(), skinProperty);

            return Pair.of(target, skinProperty);
            }).<Boolean>thenApplyAsync(pair -> {
            @Nullable Property skinProperty = pair.right();
            if (skinProperty == null) {
                return Boolean.FALSE;
            }

            /* Check whether the new skin is identical to the old one. */
                GameProfile gameProfile = pair.left();
            @Nullable ServerPlayerEntity player = ServerHelper
                .getOnlinePlayerByUuid(gameProfile.getId())
                .orElse(null);
            if (player == null) {
                return Boolean.FALSE;
            }

            // If new skin is identical to old skin, then success immediately.
            if (isSkinPropertyEqual(skinProperty, player.getGameProfile())) {
                return Boolean.TRUE;
            }

            /* Apply the skin */
            modifyGameProfile(player.getGameProfile(), skinProperty);

            /* Broadcast the game profile change. */
            broadcastGameProfileChange(player);

            return Boolean.TRUE;
        }, server)
            .orTimeout(10, TimeUnit.SECONDS)
            .exceptionally(e -> Boolean.FALSE);
    }

    private static void broadcastGameProfileChange(@NotNull ServerPlayerEntity player) {
        for (ServerPlayerEntity observer : EntityHelper.getServerWorld(player).getPlayers()) {

            /* update the tablist */
            observer.networkHandler.sendPacket(new PlayerRemoveS2CPacket(Collections.singletonList(player.getUuid())));
            observer.networkHandler.sendPacket(new PlayerListS2CPacket(PlayerListS2CPacket.Action.ADD_PLAYER, player));

            /* update the player entity */
            if (player != observer && observer.canSee(player)) {
                observer.networkHandler.sendPacket(new EntitiesDestroyS2CPacket(player.getId()));
                observer.networkHandler.sendPacket(new EntitySpawnS2CPacket(player, 0, player.getBlockPos()));

                #if MC_VER <= MC_1_21
                    observer.networkHandler.sendPacket(new EntityPositionS2CPacket(player));
                #elif MC_VER > MC_1_21
                    observer.networkHandler.sendPacket(EntityPositionS2CPacket.create(player.getId(), PlayerPosition.fromEntity(player), Set.of(), player.isOnGround()));
                #endif

                observer.networkHandler.sendPacket(new EntityTrackerUpdateS2CPacket(player.getId(), player.getDataTracker().getChangedEntries()));
                observer.networkHandler.sendPacket(new EntityPassengersSetS2CPacket(player));

            } else if (player == observer) {
                #if MC_VER <= MC_1_20_1
                observer.networkHandler.sendPacket(new PlayerRespawnS2CPacket(player.getWorld().getDimensionKey(), player.getWorld().getRegistryKey(), BiomeAccess.hashSeed(player.getServerWorld().getSeed()), player.interactionManager.getGameMode(), player.interactionManager.getPreviousGameMode(), player.getWorld().isDebugWorld(), player.getServerWorld().isFlat(), (byte) 2, player.getLastDeathPos(), player.getPortalCooldown()));
                #elif MC_VER > MC_1_20_1
                observer.networkHandler.sendPacket(new PlayerRespawnS2CPacket(player.createCommonPlayerSpawnInfo(EntityHelper.getServerWorld(player)), (byte) 2));
                #endif

                observer.networkHandler.requestTeleport(observer.getX(), observer.getY(), observer.getZ(), observer.getYaw(), observer.getPitch());

                observer.networkHandler.sendPacket(new DifficultyS2CPacket(EntityHelper.getServerWorld(observer).getDifficulty(), EntityHelper.getServerWorld(player).getLevelProperties().isDifficultyLocked()));

                #if MC_VER < MC_1_21_5
                observer.networkHandler.sendPacket(new UpdateSelectedSlotS2CPacket(observer.getInventory().selectedSlot));
                #elif MC_VER >= MC_1_21_5
                observer.networkHandler.sendPacket(new UpdateSelectedSlotS2CPacket(observer.getInventory().getSelectedSlot()));
                #endif


                observer.sendAbilitiesUpdate();
                observer.playerScreenHandler.updateToClient();
                for (StatusEffectInstance instance : observer.getStatusEffects()) {
                    #if MC_VER <= MC_1_20_4
                    observer.networkHandler.sendPacket(new EntityStatusEffectS2CPacket(observer.getId(), instance));
                    #elif MC_VER > MC_1_20_4
                    observer.networkHandler.sendPacket(new EntityStatusEffectS2CPacket(observer.getId(), instance, false));
                    #endif

                }
                observer.networkHandler.sendPacket(new EntityTrackerUpdateS2CPacket(player.getId(), player.getDataTracker().getChangedEntries()));
                observer.networkHandler.sendPacket(new ExperienceBarUpdateS2CPacket(player.experienceProgress, player.totalExperience, player.experienceLevel));
                observer.networkHandler.sendPacket(new EntityPassengersSetS2CPacket(observer));
            }
        }
    }

}
