package io.github.sakurawald.fuji.core.config.mapper.wrapper;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.PropertyMap;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.AuthlibHelper;
import java.util.Optional;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GameProfileWrapper {
    @Nullable("For an invalid online player name, the UUID is null.") UUID id;
    @NotNull String name;
    @NotNull PropertyMapWrapper properties;

    public static @NotNull GameProfileWrapper of(@Nullable UUID id, @NotNull String name) {
        return new GameProfileWrapper(id, name, PropertyMapWrapper.fromVanillaType(AuthlibHelper.makePropertyMap()));
    }

    public static @NotNull GameProfileWrapper of(@Nullable UUID id, @NotNull String name, @NotNull PropertyMap properties) {
        return new GameProfileWrapper(id, name, PropertyMapWrapper.fromVanillaType(properties));
    }

    public Optional<GameProfile> toGameProfile() {
        if (this.id == null) {
            return Optional.empty();
        }

        GameProfile gameProfile = makeGameProfile(this.id, this.name, this.properties.toVanillaType());
        return Optional.of(gameProfile);
    }

    private static @NotNull GameProfile makeGameProfile(@NotNull UUID id, @NotNull String name, @NotNull PropertyMap properties) {
        #if MC_VER < MC_1_21_9
        GameProfile gameProfile = new GameProfile(id, name);
        properties.putAll(properties);
        return gameProfile;
        #elif MC_VER >= MC_1_21_9
        // NOTE: After MC 1.21.9, the PropertyMap becomes an immutable collection.
        return new GameProfile(id, name, properties);
        #endif
    }

    public static @NotNull GameProfileWrapper fromVanillaType(@NotNull GameProfile gameProfile) {
        UUID id = AuthlibHelper.getId(gameProfile);
        String name = AuthlibHelper.getName(gameProfile);
        PropertyMap properties = AuthlibHelper.getProperties(gameProfile);
        return GameProfileWrapper.of(id, name, properties);
    }

    #if MC_VER >= MC_1_21_9
    public static @NotNull GameProfileWrapper fromVanillaType(@NotNull net.minecraft.server.PlayerConfigEntry playerConfigEntry) {
        return GameProfileWrapper.of(playerConfigEntry.comp_4422(), playerConfigEntry.comp_4423());
    }
    #endif

    #if MC_VER < MC_1_21_9
    public Optional<GameProfile> toVanillaType() {
        return toGameProfile();
    }
    #elif MC_VER >= MC_1_21_9
    public Optional<net.minecraft.server.PlayerConfigEntry> toVanillaType() {
        return toGameProfile()
            .map(net.minecraft.server.PlayerConfigEntry::new);
    }
    #endif

}
