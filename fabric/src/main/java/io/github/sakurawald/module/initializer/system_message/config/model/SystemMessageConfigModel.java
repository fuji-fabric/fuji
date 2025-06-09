package io.github.sakurawald.module.initializer.system_message.config.model;

import java.util.HashMap;
import java.util.Map;

public class SystemMessageConfigModel {
    public Map<String, String> key2value = new HashMap<>() {
        {
            this.put("commands.seed.success", "<rainbow>Seeeeeeeeeeed: %s");
        }
    };
}
