package mod.fuji.core.config.mapper.representation;

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

    public static @NotNull GameProfileIR from(@Nullable UUID id, @NotNull String name) {
        return new GameProfileIR(id, name, PropertyMapIR.fromNative(AuthlibHelper.makePropertyMap()));
    }

    public static @NotNull GameProfileIR from(@Nullable UUID id, @NotNull String name, @NotNull PropertyMap properties) {
        return new GameProfileIR(id, name, PropertyMapIR.fromNative(properties));
    }

    public static @NotNull GameProfileIR from(@NotNull Player player) {
        return from(player.getGameProfile());
    }

    public static @NotNull GameProfileIR from(@NotNull GameProfile gameProfile) {
        UUID id = AuthlibHelper.getGameProfileId(gameProfile);
        String name = AuthlibHelper.getGameProfileName(gameProfile);
        PropertyMap properties = AuthlibHelper.getGameProfileProperties(gameProfile);
        return GameProfileIR.from(id, name, properties);
    }

    #if MC_VER >= MC_1_21_9
    public static @NotNull GameProfileIR from(@NotNull net.minecraft.server.players.NameAndId playerConfigEntry) {
        return GameProfileIR.from(playerConfigEntry.id(), playerConfigEntry.name());
    }

    public static @NotNull GameProfileIR from(@NotNull net.minecraft.server.players.CachedUserNameToIdResolver.GameProfileInfo gameProfileInfo) {
        var compound = gameProfileInfo.nameAndId();
        return GameProfileIR.from(compound.id(), compound.name());
    }
    #endif

    #if MC_VER <= MC_1_21_6
    public static @NotNull GameProfileIR from(@NotNull net.minecraft.server.players.GameProfileCache.GameProfileInfo gameProfileInfo) {
        var compound = gameProfileInfo.getProfile();
        return GameProfileIR.of(compound.getId(), compound.getName());
    }
    #endif

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

    /* After MC 1.21.9, the GameProfile class splits into GameProfile and NameAndId classes.
    * The GameProfile class is the underlying class from authlib.
    * Mojang uses the NameAndId in game-level logics.
    *  */

    public Optional<GameProfile> toGameProfile() {
        if (this.id == null) {
            return Optional.empty();
        }

        GameProfile gameProfile = makeGameProfile(this.id, this.name, this.properties.toNative());
        return Optional.of(gameProfile);
    }

    #if MC_VER < MC_1_21_9
    public Optional<GameProfile> toUserProfile() {
        return toGameProfile();
    }
    #elif MC_VER >= MC_1_21_9
    public Optional<net.minecraft.server.players.NameAndId> toUserProfile() {
        return toGameProfile()
            .map(net.minecraft.server.players.NameAndId::new);
    }
    #endif

}
