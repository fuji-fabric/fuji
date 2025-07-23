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
import io.github.sakurawald.fuji.core.command.argument.adapter.impl.TeamArgumentTypeAdapter;
import io.github.sakurawald.fuji.core.config.handler.abst.BaseConfigurationHandler;
import io.github.sakurawald.fuji.module.initializer.skin.SkinInitializer;
import io.github.sakurawald.fuji.module.initializer.skin.structure.SkinDescriptor;
import io.github.sakurawald.fuji.module.initializer.skin.structure.SkinStorage;
import it.unimi.dsi.fastutil.Pair;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import lombok.Getter;
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
import net.minecraft.world.biome.source.BiomeAccess;
import org.jetbrains.annotations.NotNull;

#if MC_VER > MC_1_21
import net.minecraft.entity.player.PlayerPosition;
#endif

public class SkinService {


    @Getter
    private static final SkinStorage skinStorage = new SkinStorage();

    public static int applySkin(@NotNull ServerPlayerEntity player, @NotNull Supplier<Property> skinSupplier) {
        setSkinAsync(player.getGameProfile(), skinSupplier)
            .thenAccept(success -> {
            if (!success) {
                TextHelper.sendTextByKey(player, "skin.action.failed");
                return;
            }

            TextHelper.sendTextByKey(player, "skin.action.ok");
        });

        return CommandHelper.Return.SUCCESS;
    }

    public static void applySkin(@NotNull GameProfile gameProfile, @NotNull Property skin) {
        gameProfile.getProperties().removeAll("textures");
        gameProfile.getProperties().put("textures", skin);
    }

    public static @NotNull Property getRandomDefaultSkin() {
        return RandomUtil
            .drawList(SkinInitializer.config.model().getDefaultSkinList())
            .getSkinProperty();
    }

    public static boolean isUsingDefaultSkin(GameProfile gameProfile) {
        Optional<Property> skinProperty = gameProfile
            .getProperties()
            .get("textures")
            .stream()
            .findFirst();
        return skinProperty
            .filter($skinProperty -> SkinInitializer.config.model()
                .getDefaultSkinList()
                .stream()
                .map(SkinDescriptor::getSkinProperty)
                .anyMatch(defaultSkinProperty -> {
                    String A = PlayerHelper.getPropertyValue(defaultSkinProperty);
                    String B = PlayerHelper.getPropertyValue($skinProperty);
                    return A.equals(B);
                }))
            .isPresent();
    }

    public static boolean arePropertiesEquals(@NotNull JsonObject x, @NotNull GameProfile y) {
        Optional<Property> py = y.getProperties()
            .get("textures")
            .stream()
            .findFirst();

        if (py.isEmpty())
            return false;

        try {
            String json = new String(Base64.getDecoder().decode(PlayerHelper.getPropertyValue(py.get())), StandardCharsets.UTF_8);
            JsonObject jy = BaseConfigurationHandler.getGson().fromJson(json, JsonObject.class);
            jy.remove("timestamp");
            return x.equals(jy);
        } catch (Exception ex) {
            LogUtil.error("Can not compare skin", ex);
            return false;
        }
    }

    @SuppressWarnings("RedundantTypeArguments")
    public static CompletableFuture<Boolean> setSkinAsync(@NotNull GameProfile target, @NotNull Supplier<Property> skinSupplier) {
        MinecraftServer server = ServerHelper.getServer();

        return CompletableFuture
            .<Pair<Property, GameProfile>>supplyAsync(() -> {

            Property skinProperty = skinSupplier.get();
            LogUtil.debug("Resolved skin property from skin supplier: target = {}, skin = {}", target.getName(), skinProperty);

            if (skinProperty == null) {
                return Pair.of(null, null);
            }

            skinStorage.setSkinCache(target.getId(), Optional.of(skinProperty));

            return Pair.of(skinProperty, target);
        }).<Boolean>thenApplyAsync(pair -> {

            Optional<Property> skinProperty = Optional.ofNullable(pair.left());
            if (skinProperty.isEmpty()) {
                return Boolean.FALSE;
            }

            GameProfile gameProfile = pair.right();

            byte[] decode = Base64.getDecoder().decode(PlayerHelper.getPropertyValue(skinProperty.get()));
            JsonObject newSkinJson = BaseConfigurationHandler.getGson().fromJson(new String(decode, StandardCharsets.UTF_8), JsonObject.class);
            newSkinJson.remove("timestamp");

                ServerPlayerEntity player = server.getPlayerManager().getPlayer(gameProfile.getId());

                /* skip identical skin */
                if (player == null || arePropertiesEquals(newSkinJson, player.getGameProfile())) {
                    return Boolean.FALSE;
                }

                /* apply the skin */
                applySkin(player.getGameProfile(), skinProperty.get());

                /* broadcast the change */
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

            return Boolean.TRUE;
        }, server)
            .orTimeout(10, TimeUnit.SECONDS)
            .exceptionally(e -> Boolean.FALSE);
    }
}
