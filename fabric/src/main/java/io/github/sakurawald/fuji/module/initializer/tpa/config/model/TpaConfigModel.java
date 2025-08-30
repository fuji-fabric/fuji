package io.github.sakurawald.fuji.module.initializer.tpa.config.model;

import com.google.gson.annotations.SerializedName;
import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.core.job.impl.PlaySoundJob;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class TpaConfigModel {

    @SerializedName(value = "request_timeout", alternate = "timeout")
    @Document(id = 1751826543066L, value = "Expiration duration seconds for each tpa request.")
    int requestTimeout = 300;

    PlaySoundJob.PlaySoundJobSetup mentionPlayer = new PlaySoundJob.PlaySoundJobSetup();
}
