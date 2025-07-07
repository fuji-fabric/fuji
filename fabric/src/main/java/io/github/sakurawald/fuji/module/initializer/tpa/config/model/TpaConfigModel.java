package io.github.sakurawald.fuji.module.initializer.tpa.config.model;

import com.google.gson.annotations.SerializedName;
import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.core.job.impl.PlaySoundJob;

public class TpaConfigModel {

    @SerializedName(value = "request_timeout", alternate = "timeout")
    @Document(id = 1751826543066L, value = "This is the timeout.")
    public int request_timeout = 300;

    public PlaySoundJob.PlaySoundJobSetup mention_player = new PlaySoundJob.PlaySoundJobSetup();
}
