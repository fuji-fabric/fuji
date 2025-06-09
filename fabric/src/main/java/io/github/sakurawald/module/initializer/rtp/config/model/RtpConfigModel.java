package io.github.sakurawald.module.initializer.rtp.config.model;

import io.github.sakurawald.core.structure.TeleportSetup;

import java.util.ArrayList;
import java.util.List;

public class RtpConfigModel {

    public Setup setup = new Setup();

    public static class Setup {
        public List<TeleportSetup> dimension = new ArrayList<>() {

            {
                this.add(new TeleportSetup("minecraft:overworld", 0, 0, false, 1000, 5000, -64, 320, 16));
                this.add(new TeleportSetup("minecraft:the_nether", 0, 0, false, 1000, 5000, 0, 128, 16));
                this.add(new TeleportSetup("minecraft:the_end", 0, 0, false, 1000, 5000, 0, 256, 16));
                this.add(new TeleportSetup("fuji:overworld", 0, 0, false, 1000, 5000, -64, 320, 16));
                this.add(new TeleportSetup("fuji:the_nether", 0, 0, false, 1000, 5000, 0, 128, 16));
                this.add(new TeleportSetup("fuji:the_end", 0, 0, false, 0, 48, 0, 256, 16));
            }

        };
    }
}
