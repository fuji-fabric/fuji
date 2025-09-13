package io.github.sakurawald.fuji.module.initializer.rtp.config.model;

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
    Setup setup = new Setup();

    @Data
    @NoArgsConstructor
    public static class Setup {
        List<RandomTeleportSettings> dimension = new ArrayList<>() {

            {
                this.add(new RandomTeleportSettings("minecraft:overworld", 0, 0, false, 1000, 5000, -64, 320, 16, new RandomTeleportSettings.Biomes()));
                this.add(new RandomTeleportSettings("minecraft:the_nether", 0, 0, false, 1000, 5000, 0, 128, 16, new RandomTeleportSettings.Biomes()));
                this.add(new RandomTeleportSettings("minecraft:the_end", 0, 0, false, 1000, 5000, 0, 256, 16, new RandomTeleportSettings.Biomes()));
                this.add(new RandomTeleportSettings("fuji:overworld", 0, 0, false, 1000, 5000, -64, 320, 16, new RandomTeleportSettings.Biomes()));
                this.add(new RandomTeleportSettings("fuji:the_nether", 0, 0, false, 1000, 5000, 0, 128, 16, new RandomTeleportSettings.Biomes()));
                this.add(new RandomTeleportSettings("fuji:the_end", 0, 0, false, 0, 48, 0, 256, 16, new RandomTeleportSettings.Biomes()));
            }

        };
    }
}
