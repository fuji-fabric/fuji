package io.github.sakurawald.module.initializer.teleport_warmup.config.model;

import com.google.gson.annotations.SerializedName;
import io.github.sakurawald.core.annotation.Document;
import io.github.sakurawald.core.manager.impl.bossbar.structure.Interruptible;

import java.util.HashSet;
import java.util.Set;

public class TeleportWarmupConfigModel {

    @Document("""
        The `warmup seconds` for `all` teleports.
        """)
    public int warmup_second = 3;

    public Interruptible interruptible = new Interruptible(true, 1, true, true);

    public Dimension dimension = new Dimension();
    public static class Dimension {

        @Document("""
            Define the `effective dimensions` for `teleport warmup`.
            """)
        @SerializedName(value = "effective_dimensions", alternate = {"list", "blacklist"})
        public Set<String> effective_dimensions = new HashSet<>() {
            {
                this.add("minecraft:overworld");
                this.add("minecraft:the_nether");
                this.add("minecraft:the_end");
            }
        };
    }
}
