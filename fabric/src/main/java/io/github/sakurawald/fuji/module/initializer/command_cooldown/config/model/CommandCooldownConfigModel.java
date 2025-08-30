package io.github.sakurawald.fuji.module.initializer.command_cooldown.config.model;

import com.google.gson.annotations.SerializedName;
import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.module.initializer.command_cooldown.structure.NamedCooldownDescriptor;

import java.util.HashMap;
import java.util.Map;

public class CommandCooldownConfigModel {

    @Document(id = 1751826371102L, value = """
        The `unnamed cooldown` is applied `per-player`.

        Define the `regex` expression to match the `target command`.
        And the `cooldown ms` for that `target command`.
        """)
    @SerializedName(value = "unnamed_cooldown", alternate = "regex2ms")
    public Map<String, Long> unnamed_cooldown = new HashMap<>() {
        {
            this.put("chunks.*", 60 * 1000L);
            this.put("rtp.*", 60 * 1000L);
            this.put("download.*", 120 * 1000L);
        }
    };

    @Document(id = 1751826373554L, value = """
        The `named cooldown` is created by `/command-cooldown create` command.
        """)
    public NamedCooldown namedCooldown = new NamedCooldown();
    public static class NamedCooldown {
        public Map<String, NamedCooldownDescriptor> list = new HashMap<>();
    }
}
