package mod.fuji.module.initializer.teleport_warmup.config.model;

import com.google.gson.annotations.SerializedName;
import mod.fuji.core.document.annotation.Document;
import mod.fuji.core.service.bossbar.structure.Interruptible;

import java.util.HashSet;
import java.util.Set;

public class TeleportWarmupConfigModel {

    @Document(id = 1751826785758L, value = """
        The `warmup seconds` for `all` teleports.
        """)
    public double warmup_second = 3;

    public Interruptible interruptible = new Interruptible(true, 1, true, true);

    public Dimension dimension = new Dimension();
    public static class Dimension {

        @Document(id = 1751826788564L, value = """
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
