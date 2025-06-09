package io.github.sakurawald.module.initializer.placeholder.structure;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.github.sakurawald.core.auxiliary.minecraft.ServerHelper;
import lombok.Getter;
import lombok.ToString;
import net.minecraft.util.WorldSavePath;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.regex.Pattern;

@ToString
public class SumUpPlaceholder {
    public static final HashMap<String, SumUpPlaceholder> uuid2stats = new HashMap<>();
    private static final int CM_TO_KM_DIVISOR = 100 * 1000;
    private static final int GT_TO_H_DIVISOR = 20 * 3600;

    @Getter
    private static SumUpPlaceholder ofServer;

    public int playtime;
    public int mined;
    public int placed;
    public int killed;
    public int moved;

    private static Path getStatPath() {
        return ServerHelper.getServer().getSavePath(WorldSavePath.STATS);
    }

    @SuppressWarnings("UnnecessaryLocalVariable")
    public static SumUpPlaceholder ofPlayer(String uuid) {
        if (uuid2stats.containsKey(uuid)) {
            return uuid2stats.get(uuid);
        }

        SumUpPlaceholder ret = new SumUpPlaceholder();

        try {
            // get player statistics
            File file = getStatPath().resolve(uuid + ".json").toFile();
            if (!file.exists()) return ret;

            JsonElement jsonElement = JsonParser.parseReader(new FileReader(file));
            if (!jsonElement.isJsonObject()) {
                // return a dummy placeholder, if the related statistics file is not write into the storage, for a new player.
                return new SumUpPlaceholder();
            }
            JsonObject json = jsonElement.getAsJsonObject();
            JsonObject stats = json.getAsJsonObject("stats");
            if (stats == null) return ret;

            int mined_all = sumUpStats(stats.getAsJsonObject("minecraft:mined"), ".*");
            int used_all = sumUpStats(stats.getAsJsonObject("minecraft:used"), ".*");
            JsonObject custom = stats.getAsJsonObject("minecraft:custom");
            if (custom == null) return ret;
            int moved_all = sumUpStats(custom, ".+_cm") / CM_TO_KM_DIVISOR;
            JsonElement mobKills = custom.get("minecraft:mob_kills");
            int mob_kills = mobKills == null ? 0 : mobKills.getAsInt();
            JsonElement playTime = custom.get("minecraft:play_time");
            int play_time = (playTime == null ? 0 : playTime.getAsInt()) / GT_TO_H_DIVISOR;

            // set main-stats
            ret.playtime = play_time;
            ret.mined = mined_all;
            ret.placed = used_all;
            ret.killed = mob_kills;
            ret.moved = moved_all;

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // save
        uuid2stats.put(uuid, ret);
        return ret;
    }

    public static @NotNull SumUpPlaceholder ofServer() {
        SumUpPlaceholder ofServer = new SumUpPlaceholder();
        File file = getStatPath().toFile();
        File[] files = file.listFiles();
        if (files == null) return ofServer;

        for (File playerStatFile : files) {
            String uuid = playerStatFile.getName().replace(".json", "");
            SumUpPlaceholder playerMainStats = ofPlayer(uuid);
            ofServer.plus(playerMainStats);
        }

        // save
        SumUpPlaceholder.ofServer = ofServer;

        return ofServer;
    }

    private static int sumUpStats(@Nullable JsonObject jsonObject, @NotNull String regex) {
        if (jsonObject == null) return 0;
        int count = 0;
        Pattern pattern = Pattern.compile(regex);
        for (String key : jsonObject.keySet()) {
            if (pattern.matcher(key).matches()) {
                count += jsonObject.get(key).getAsInt();
            }
        }
        return count;
    }

    public void plus(@NotNull SumUpPlaceholder other) {
        this.playtime += other.playtime;
        this.mined += other.mined;
        this.placed += other.placed;
        this.killed += other.killed;
        this.moved += other.moved;
    }
}
