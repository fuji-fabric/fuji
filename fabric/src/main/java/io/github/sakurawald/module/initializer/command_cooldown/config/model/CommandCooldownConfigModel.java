package io.github.sakurawald.module.initializer.command_cooldown.config.model;

import com.google.gson.annotations.SerializedName;
import io.github.sakurawald.module.initializer.command_cooldown.structure.CommandCooldown;

import java.util.HashMap;
import java.util.Map;

public class CommandCooldownConfigModel {

    @SerializedName(value = "unnamed_cooldown", alternate = "regex2ms")
    public Map<String, Long> unnamed_cooldown = new HashMap<>() {
        {
            this.put("world tp (overworld|the_nether|the_end)", 120 * 1000L);
            this.put("chunks\\s*", 60 * 1000L);
            this.put("rtp\\s*", 60 * 1000L);
            this.put("download\\s*", 120 * 1000L);
            this.put("heal\\s*", 300 * 1000L);
            this.put("repair\\s*", 300 * 1000L);
        }
    };

    public NamedCooldown namedCooldown = new NamedCooldown();

    public static class NamedCooldown {
        public Map<String, CommandCooldown> list = new HashMap<>();
    }
}
