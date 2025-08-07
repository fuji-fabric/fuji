package io.github.sakurawald.fuji.module.initializer.chat.display.config.model;

import io.github.sakurawald.fuji.core.document.annotation.Document;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ChatDisplayConfigModel {
    @Document(id = 1751826638318L, value = "The expiration duration for each created `display`.")
    int expirationDurationS = 3600;
}
