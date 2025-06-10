package io.github.sakurawald.module.initializer.skin.structure;

import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import io.github.sakurawald.core.annotation.Cite;
import io.github.sakurawald.core.auxiliary.LogUtil;
import io.github.sakurawald.core.config.handler.abst.BaseConfigurationHandler;
import it.unimi.dsi.fastutil.Pair;
import lombok.Getter;
import net.minecraft.entity.effect.StatusEffectInstance;
#if MC_VER > MC_1_21
import net.minecraft.entity.player.PlayerPosition;
#endif
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
#if MC_VER >= MC_1_21_4
import net.minecraft.network.packet.s2c.play.UpdateSelectedSlotS2CPacket;
#endif
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@Cite("https://github.com/Suiranoil/SkinRestorer")
public class SkinRestorer {

    @Getter
    private static final SkinStorage skinStorage = new SkinStorage();

    public static CompletableFuture<Pair<Collection<ServerPlayerEntity>, Collection<GameProfile>>> setSkinAsync(@NotNull MinecraftServer server, @NotNull Collection<GameProfile> targets, @NotNull Supplier<Property> skinSupplier) {
        return CompletableFuture.<Pair<Property, Collection<GameProfile>>>supplyAsync(() -> {
            Set<GameProfile> acceptedProfiles = new HashSet<>();
            Property skin = skinSupplier.get();

            LogUtil.debug("skinSupplier.get() -> skin = {}", skin);
            if (skin == null) {
                LogUtil.debug("cannot get the skin for {}", targets.stream().findFirst().orElseThrow().getName());
                return Pair.of(null, Collections.emptySet());
            }

            for (GameProfile profile : targets) {
                SkinRestorer.getSkinStorage().setSkin(profile.getId(), skin);
                acceptedProfiles.add(profile);
            }

            return Pair.of(skin, acceptedProfiles);
        }).<Pair<Collection<ServerPlayerEntity>, Collection<GameProfile>>>thenApplyAsync(pair -> {

            Property skin = pair.left();
            if (skin == null)
                return Pair.of(Collections.emptySet(), Collections.emptySet());

            Collection<GameProfile> acceptedProfiles = pair.right();
            Set<ServerPlayerEntity> acceptedPlayers = new HashSet<>();

            JsonObject newSkinJson = BaseConfigurationHandler.getGson().fromJson(new String(Base64.getDecoder().decode(skin.value()), StandardCharsets.UTF_8), JsonObject.class);
            newSkinJson.remove("timestamp");

            for (GameProfile profile : acceptedProfiles) {
                ServerPlayerEntity player = server.getPlayerManager().getPlayer(profile.getId());

                /* skip identical skin */
                if (player == null || arePropertiesEquals(newSkinJson, player.getGameProfile()))
                    continue;

                /* apply the skin */
                applySkin(player.getGameProfile(), skin);

                /* broadcast the change */
                for (ServerPlayerEntity observer : player.getServerWorld().getPlayers()) {

                    /* update the tablist */
                    observer.networkHandler.sendPacket(new PlayerRemoveS2CPacket(Collections.singletonList(player.getUuid())));
                    observer.networkHandler.sendPacket(new PlayerListS2CPacket(PlayerListS2CPacket.Action.ADD_PLAYER, player));

                    /* update the player entity */
                    if (player != observer && observer.canSee(player)) {
                        observer.networkHandler.sendPacket(new EntitiesDestroyS2CPacket(player.getId()));
                        observer.networkHandler.sendPacket(new EntitySpawnS2CPacket(player, 0, player.getBlockPos()));

                        #if MC_VER == MC_1_21
                            observer.networkHandler.sendPacket(new EntityPositionS2CPacket(player));
                        #elif MC_VER > MC_1_21
                            observer.networkHandler.sendPacket(EntityPositionS2CPacket.create(player.getId(), PlayerPosition.fromEntity(player), Set.of(), player.isOnGround()));
                        #endif

                        observer.networkHandler.sendPacket(new EntityTrackerUpdateS2CPacket(player.getId(), player.getDataTracker().getChangedEntries()));
                        observer.networkHandler.sendPacket(new EntityPassengersSetS2CPacket(player));

                    } else if (player == observer) {
                        observer.networkHandler.sendPacket(new PlayerRespawnS2CPacket(player.createCommonPlayerSpawnInfo(player.getServerWorld()), (byte) 2));
                        observer.networkHandler.requestTeleport(observer.getX(), observer.getY(), observer.getZ(), observer.getYaw(), observer.getPitch());

                        observer.networkHandler.sendPacket(new DifficultyS2CPacket(observer.getServerWorld().getDifficulty(), player.getServerWorld().getLevelProperties().isDifficultyLocked()));

                        #if MC_VER == MC_1_21_4
                        observer.networkHandler.sendPacket(new UpdateSelectedSlotS2CPacket(observer.getInventory().selectedSlot));
                        #elif MC_VER == MC_1_21_5
                        observer.networkHandler.sendPacket(new UpdateSelectedSlotS2CPacket(observer.getInventory().getSelectedSlot()));
                        #endif


                        observer.sendAbilitiesUpdate();
                        observer.playerScreenHandler.updateToClient();
                        for (StatusEffectInstance instance : observer.getStatusEffects()) {
                            observer.networkHandler.sendPacket(new EntityStatusEffectS2CPacket(observer.getId(), instance, false));
                        }
                        observer.networkHandler.sendPacket(new EntityTrackerUpdateS2CPacket(player.getId(), player.getDataTracker().getChangedEntries()));
                        observer.networkHandler.sendPacket(new ExperienceBarUpdateS2CPacket(player.experienceProgress, player.totalExperience, player.experienceLevel));
                        observer.networkHandler.sendPacket(new EntityPassengersSetS2CPacket(observer));
                    }
                }
                acceptedPlayers.add(player);
            }

            return Pair.of(acceptedPlayers, acceptedProfiles);
        }, server).orTimeout(10, TimeUnit.SECONDS).exceptionally(e -> Pair.of(Collections.emptySet(), Collections.emptySet()));
    }

    public static void applySkin(@NotNull GameProfile gameProfile, Property skin) {
        gameProfile.getProperties().removeAll("textures");
        gameProfile.getProperties().put("textures", skin);
    }

    private static boolean arePropertiesEquals(@NotNull JsonObject x, @NotNull GameProfile y) {
        Property py = y.getProperties().get("textures").stream().findFirst().orElse(null);
        if (py == null)
            return false;

        try {
            JsonObject jy = BaseConfigurationHandler.getGson().fromJson(new String(Base64.getDecoder().decode(py.value()), StandardCharsets.UTF_8), JsonObject.class);
            jy.remove("timestamp");
            return x.equals(jy);
        } catch (Exception ex) {
            LogUtil.error("can not compare skin", ex);
            return false;
        }
    }

}
