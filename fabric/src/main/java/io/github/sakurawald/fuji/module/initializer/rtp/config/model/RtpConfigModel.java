package io.github.sakurawald.fuji.module.initializer.rtp.config.model;

import com.google.gson.annotations.SerializedName;
import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.core.service.random_teleport.structure.RandomTeleportSettings;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class RtpConfigModel {

    @Document(id = 1751826332128L, value = """
        Define `rtp` setup for each `dimension`.
        """)
    @SerializedName(value = "dimensions", alternate = "setup")
    Dimensions dimensions = new Dimensions();

    @Data
    @NoArgsConstructor
    public static class Dimensions {
        @SerializedName(value = "settings", alternate = "dimension")
        List<RandomTeleportSettings> settings = new ArrayList<>() {

            {
                this.add(new RandomTeleportSettings(true, "minecraft:overworld", 0, 0, false, 1000, 5000, -64, 320, 16, 20 * 10, 6000, new RandomTeleportSettings.Biomes(), new RandomTeleportSettings.Blocks()));
                this.add(new RandomTeleportSettings(true, "minecraft:the_nether", 0, 0, false, 1000, 5000, 0, 128, 16, 20 * 10, 6000, new RandomTeleportSettings.Biomes(), new RandomTeleportSettings.Blocks()));
                this.add(new RandomTeleportSettings(true, "minecraft:the_end", 0, 0, false, 1000, 5000, 0, 256, 16, 20 * 10, 6000, new RandomTeleportSettings.Biomes(), new RandomTeleportSettings.Blocks()));
                this.add(new RandomTeleportSettings(true, "fuji:overworld", 0, 0, false, 1000, 5000, -64, 320, 16, 20 * 10, 6000, new RandomTeleportSettings.Biomes(), new RandomTeleportSettings.Blocks()));
                this.add(new RandomTeleportSettings(true, "fuji:the_nether", 0, 0, false, 1000, 5000, 0, 128, 16, 20 * 10, 6000, new RandomTeleportSettings.Biomes(), new RandomTeleportSettings.Blocks()));
                this.add(new RandomTeleportSettings(true, "fuji:the_end", 0, 0, false, 0, 48, 0, 256, 16, 20 * 10, 6000, new RandomTeleportSettings.Biomes(), new RandomTeleportSettings.Blocks()));
            }

        };
    }
}
