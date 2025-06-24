package io.github.sakurawald.module.initializer.chat.display.config.model;

import io.github.sakurawald.core.annotation.Document;

public class ChatDisplayConfigModel {
    @Document("The expiration duration for each created `display`.")
    public int expiration_duration_s = 3600;
}
