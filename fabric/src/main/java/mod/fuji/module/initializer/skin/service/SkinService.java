package mod.fuji.module.initializer.skin.service;

import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import mod.fuji.core.auxiliary.LogUtil;
import mod.fuji.core.auxiliary.RandomUtil;
import mod.fuji.core.auxiliary.minecraft.AuthlibHelper;
import mod.fuji.core.auxiliary.minecraft.CommandHelper;
import mod.fuji.core.auxiliary.minecraft.PlayerHelper;
import mod.fuji.core.auxiliary.minecraft.ServerHelper;
import mod.fuji.core.auxiliary.minecraft.TextHelper;
import mod.fuji.core.config.mapper.GsonMapper;
import mod.fuji.module.initializer.skin.SkinInitializer;
import mod.fuji.core.config.mapper.wrapper.PropertyWrapper;
import mod.fuji.module.initializer.skin.structure.SkinDataNode;
import mod.fuji.module.initializer.skin.structure.SkinDescriptor;
import mod.fuji.module.initializer.skin.structure.SkinStorage;
import mod.fuji.module.initializer.skin.structure.SkinSyncer;
import it.unimi.dsi.fastutil.Pair;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


public class SkinService {

    public static @NotNull Property getEffectiveSkin(GameProfile gameProfile) {
        return SkinStorage.withSkinData(gameProfile, SkinDataNode::getSkinProperty).toVanillaType();
    }

    @SuppressWarnings("FutureReturnValueIgnored")
    public static int changeSkin(@NotNull ServerPlayer player, @NotNull Supplier<Property> skinSupplier) {
        changeSkinAsync(player.getGameProfile(), skinSupplier)
            .thenAccept(success -> {

                if (!success) {
                    TextHelper.sendTextByKey(player, "skin.change_skin.failed");
                    return;
                }

                TextHelper.sendTextByKey(player, "skin.change_skin.success");
            });

        return CommandHelper.Return.SUCCESS;
    }

    public static @NotNull List<SkinDescriptor> getDefaultSkinList() {
        return SkinInitializer.config.model().getDefaultSkin().getDefaultSkinList();
    }

    public static Optional<SkinDescriptor> findSkinDescriptor(String skinName) {
        return getDefaultSkinList()
            .stream()
            .filter(it -> it.getSkinName().equals(skinName))
            .findFirst();
    }

    public static @NotNull Property getPreferredDefaultSkin() {
        /* Get the preferred default skin. */
        String preferredDefaultSkinNameForNewPlayers = SkinInitializer.config.model().getDefaultSkin().getPreferredSkinName();
        Optional<SkinDescriptor> preferredDefaultSkin = getDefaultSkinList()
            .stream()
            .filter(it -> it.getSkinName().contains(preferredDefaultSkinNameForNewPlayers))
            .findFirst();

        /* Get a random default skin. */
        return preferredDefaultSkin
            .map(skinDescriptor -> skinDescriptor.getSkinProperty().toVanillaType()).orElseGet(SkinService::getRandomDefaultSkin);
    }

    public static Property getRandomDefaultSkin() {
        return RandomUtil
            .drawList(getDefaultSkinList())
            .getSkinProperty()
            .toVanillaType();
    }

    private static boolean isSkinPropertyEqual(@NotNull Property x, @NotNull GameProfile y) {
        try {
            /* Make the x json object. */
            JsonObject xJsonObject = makeComparableJsonObjectFromSkinProperty(x);

            /* Make the y json object. */
            Optional<Property> py = AuthlibHelper.getProperties(y)
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
        String jsonString = new String(Base64.getDecoder().decode(AuthlibHelper.getPropertyValue(property)), StandardCharsets.UTF_8);
        JsonObject jsonObject = GsonMapper.fromJson(jsonString, JsonObject.class);
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
            LogUtil.debug("Resolved skin property from skin supplier: target = {}, skin = {}", AuthlibHelper.getName(target), skinProperty);
            if (skinProperty == null) {
                throw new IllegalStateException("Failed to resolve skin property from skin supplier.");
            }

            /* Update the skin data. */
            SkinStorage.withSkinData(target, node -> {
                node.setSkinProperty(PropertyWrapper.fromVanillaType(skinProperty));
                return null;
            });

            return Pair.of(target, skinProperty);
            }).<Boolean>thenApplyAsync(pair -> {
            @Nullable Property skinProperty = pair.right();
            if (skinProperty == null) {
                return Boolean.FALSE;
            }

            /* Check whether the new skin is identical to the old one. */
                GameProfile gameProfile = pair.left();
            @Nullable ServerPlayer player = PlayerHelper.Lookup
                .getOnlinePlayerByUuid(AuthlibHelper.getId(gameProfile))
                .orElse(null);
            if (player == null) {
                return Boolean.FALSE;
            }

            // If new skin is identical to old skin, then success immediately.
            if (isSkinPropertyEqual(skinProperty, player.getGameProfile())) {
                return Boolean.TRUE;
            }

            /* Apply the skin */
            AuthlibHelper.modifyGameProfile(player.getGameProfile(), skinProperty);

            /* Broadcast the game profile change. */
            SkinSyncer.broadcastGameProfileChange(player);

            return Boolean.TRUE;
        }, server)
            .orTimeout(10, TimeUnit.SECONDS)
            .exceptionally(e -> Boolean.FALSE);
    }

}
