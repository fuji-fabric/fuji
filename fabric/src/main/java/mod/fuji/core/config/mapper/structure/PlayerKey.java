package mod.fuji.core.config.mapper.structure;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import lombok.Data;
import mod.fuji.core.auxiliary.MapUtil;
import mod.fuji.core.auxiliary.minecraft.AuthlibHelper;
import mod.fuji.core.auxiliary.minecraft.PlayerHelper;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

@Data(staticConstructor = "of")
public class PlayerKey {

    /**
     * A PlayerKey is treated as a String value.
     * It's used in Map structure, to associate per-player data.
     * The string value of a PlayerKey will only be updated when an access operation actually happens.
     */
    @NotNull String playerUuidOrPlayerName;

    public static @NotNull PlayerKey from(@NotNull ServerPlayer player) {
        UUID playerUUID = PlayerHelper.getPlayerUUID(player);
        return from(playerUUID);
    }

    public static @NotNull PlayerKey from(@NotNull UUID uuid) {
        String uuidString = uuid.toString();
        return of(uuidString);
    }

    /**
     * Given a player name, there may not be an associated UUID for that name.
     */
    public static Optional<PlayerKey> from(@NotNull String playerName) {
        return PlayerHelper.Cache
            .getOfflineGameProfileByName(playerName)
            .map(AuthlibHelper::getGameProfileId)
            .map(UUID::toString)
            .map(PlayerKey::of);
    }

    @Override
    public String toString() {
        // NOTE: Override the toString() to provide the effective string value for gson mapper.
        return this.playerUuidOrPlayerName;
    }

    public static <V> Optional<V> computeValueByPlayerName(@NotNull Map<PlayerKey, V> map, @NotNull String playerName, Function<? super PlayerKey, ? extends V> mappingFunction) {
        /* Make player key. */
        Optional<PlayerKey> playerKey = PlayerHelper.Cache
            .getOfflineGameProfileByName(playerName)
            .map(profile -> {
                @NotNull String profileName = AuthlibHelper.getGameProfileName(profile);
                @NotNull UUID profileId = AuthlibHelper.getGameProfileId(profile);

                /* Migrate the key from profile name to profile UUID automatically. */
                PlayerKey oldKey = PlayerKey.of(profileName);
                if (map.containsKey(oldKey)) {
                    PlayerKey newKey = PlayerKey.of(profileId.toString());
                    MapUtil.renameKey(map, oldKey, newKey);
                }

                /* Map the player name into the player key. */
                return PlayerKey.from(profileId);
            });

        /* Use player key to get the value. */
        return playerKey
            .map($playerKey -> computeValueByPlayerKey(map, $playerKey, mappingFunction));
    }

    public static <V> V computeValueByPlayerNameOrThrow(@NotNull Map<PlayerKey, V> map, @NotNull String playerName, Function<? super PlayerKey, ? extends V> mappingFunction) {
        return computeValueByPlayerName(map, playerName, mappingFunction)
            .orElseThrow(() -> new IllegalStateException("Cannot find the associated UUID for username %s in usercache.json file.".formatted(playerName)));
    }

    public static <V> V computeValueByPlayerKey(@NotNull Map<PlayerKey, V> map, @NotNull PlayerKey playerKey, Function<? super PlayerKey, ? extends V> mappingFunction) {
        return map.computeIfAbsent(playerKey, mappingFunction);
    }

    public static final class PlayerUUIDTypeAdapter extends TypeAdapter<PlayerKey> {

        @Override
        public void write(JsonWriter out, PlayerKey value) throws IOException {
            if (value == null) {
                out.nullValue();
                return;
            }
            String jsonValue = value.getPlayerUuidOrPlayerName();
            out.value(jsonValue);
        }

        @Override
        public PlayerKey read(JsonReader in) throws IOException {
            if (in.peek() == JsonToken.NULL) {
                in.nextNull();
                return null;
            }
            String jsonValue = in.nextString();
            return PlayerKey.of(jsonValue);
        }
    }

}
