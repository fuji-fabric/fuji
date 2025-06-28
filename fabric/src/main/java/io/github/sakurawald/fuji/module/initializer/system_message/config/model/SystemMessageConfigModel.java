package io.github.sakurawald.fuji.module.initializer.system_message.config.model;

import io.github.sakurawald.fuji.core.annotation.Document;

import java.util.HashMap;
import java.util.Map;

public class SystemMessageConfigModel {
    @Document("""
        The defined `translatable text` override.

        The `key` is `translatable key`.
        The `value` is `the overridden text`.

        If the `value` is `null`, then it means `cancel` the sending of this message.

        """)
    public Map<String, String> key2value = new HashMap<>() {
        {
            this.put("commands.seed.success", "<rainbow>Seeeeeeeeeeed: %s");
        }
    };
}
