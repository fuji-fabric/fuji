package io.github.sakurawald.fuji.core.config.mapper.wrapper;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.PropertyMap;
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
    @NotNull PropertyMap properties = new PropertyMap();

    public GameProfileWrapper(@Nullable UUID id, @NotNull String name) {
        this.id = id;
        this.name = name;
    }

    public static @NotNull GameProfileWrapper fromVanillaType(@NotNull GameProfile gameProfile) {
        GameProfileWrapper gameProfileWrapper = new GameProfileWrapper(gameProfile.getId(), gameProfile.getName());
        gameProfileWrapper.properties.putAll(gameProfile.getProperties());
        return gameProfileWrapper;
    }

    public Optional<GameProfile> toVanillaType() {
        if (this.id == null) {
            return Optional.empty();
        }

        GameProfile gameProfile = new GameProfile(this.id, this.name);
        gameProfile.getProperties().putAll(this.properties);
        return Optional.of(gameProfile);
    }
}
