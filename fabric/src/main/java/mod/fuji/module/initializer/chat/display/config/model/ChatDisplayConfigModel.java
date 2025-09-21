package mod.fuji.module.initializer.chat.display.config.model;

import com.google.gson.annotations.SerializedName;
import mod.fuji.core.document.annotation.Document;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ChatDisplayConfigModel {

    @Document(id = 1751826638318L, value = "The expiration duration for each created `display`.")
    @SerializedName(value = "expiration_duration_seconds", alternate = "expiration_duration_s")
    int expirationDurationSeconds = 3600;
}
