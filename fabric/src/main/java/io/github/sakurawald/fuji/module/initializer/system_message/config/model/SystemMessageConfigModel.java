package io.github.sakurawald.fuji.module.initializer.system_message.config.model;

import com.google.gson.annotations.SerializedName;
import io.github.sakurawald.fuji.core.document.annotation.Document;

import java.util.HashMap;
import java.util.Map;

public class SystemMessageConfigModel {
    @Document(id = 1751824921414L, value = """
        The defined `translatable text` override.

        The `key` is `translatable key`.
        The `value` is `the overridden text`.

        If the `value` is `null`, then it means `cancel` the sending of this message.

        """)
    @SerializedName(value = "rules", alternate = "key2value")
    public Map<String, String> rules = new HashMap<>() {
        {
            this.put("commands.seed.success", "<rainbow>Seeeeeeeeeeed: %s");
        }
    };
}
