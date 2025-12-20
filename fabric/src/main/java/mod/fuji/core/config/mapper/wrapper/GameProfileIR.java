package mod.fuji.core.config.mapper.wrapper;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.PropertyMap;
import mod.fuji.core.auxiliary.minecraft.AuthlibHelper;
import java.util.Optional;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GameProfileIR {

    /**
     * The UUID of the <code>user account</code>.
     * Note that it is different between <code>online account</code> and <code>offline account</code>.
     */
    @Nullable("For an invalid online player name, the UUID is null.") UUID id;

    /**
     * The <code>username</code> of the <code>user account</code>.
     * This field is <code>case-sensitive</code>.
     */
    @NotNull String name;

    @NotNull PropertyMapIR properties;

    public static @NotNull GameProfileIR of(@Nullable UUID id, @NotNull String name) {
        return new GameProfileIR(id, name, PropertyMapIR.fromVanillaType(AuthlibHelper.makePropertyMap()));
    }

    public static @NotNull GameProfileIR of(@Nullable UUID id, @NotNull String name, @NotNull PropertyMap properties) {
        return new GameProfileIR(id, name, PropertyMapIR.fromVanillaType(properties));
    }

    public static @NotNull GameProfileIR of(@NotNull Player player) {
        return fromVanillaType(player.getGameProfile());
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

    #if MC_VER >= MC_1_21_9
    public static @NotNull GameProfile toGameProfile(net.minecraft.server.players.NameAndId playerConfigEntry) {
        String name = playerConfigEntry.name();
        UUID id = playerConfigEntry.id();
        return new GameProfile(id, name);
    }
    #endif

    @SuppressWarnings("unused")
    public static @NotNull GameProfile toGameProfile(@NotNull GameProfile gameProfile) {
        return gameProfile;
    }


    public static @NotNull GameProfileIR fromVanillaType(@NotNull GameProfile gameProfile) {
        UUID id = AuthlibHelper.getId(gameProfile);
        String name = AuthlibHelper.getName(gameProfile);
        PropertyMap properties = AuthlibHelper.getProperties(gameProfile);
        return GameProfileIR.of(id, name, properties);
    }

    #if MC_VER >= MC_1_21_9
    public static @NotNull GameProfileIR fromVanillaType(@NotNull net.minecraft.server.players.NameAndId playerConfigEntry) {
        return GameProfileIR.of(playerConfigEntry.id(), playerConfigEntry.name());
    }

    public static @NotNull GameProfileIR fromVanillaType(@NotNull net.minecraft.server.players.CachedUserNameToIdResolver.GameProfileInfo gameProfileInfo) {
        var compound = gameProfileInfo.nameAndId();
        return GameProfileIR.of(compound.id(), compound.name());
    }
    #endif

    #if MC_VER <= MC_1_21_6
    public static @NotNull GameProfileWrapper fromVanillaType(@NotNull net.minecraft.server.players.GameProfileCache.GameProfileInfo gameProfileInfo) {
        var compound = gameProfileInfo.getProfile();
        return GameProfileWrapper.of(compound.getId(), compound.getName());
    }
    #endif

    #if MC_VER < MC_1_21_9
    public Optional<GameProfile> toVanillaType() {
        return toGameProfile();
    }
    #elif MC_VER >= MC_1_21_9
    public Optional<net.minecraft.server.players.NameAndId> toVanillaType() {
        return toGameProfile()
            .map(net.minecraft.server.players.NameAndId::new);
    }
    #endif
}
