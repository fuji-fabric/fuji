package io.github.sakurawald.fuji.module.initializer.chat.display.config.model;

import io.github.sakurawald.fuji.core.document.annotation.Document;

public class ChatDisplayConfigModel {
    @Document("The expiration duration for each created `display`.")
    public int expiration_duration_s = 3600;
}
