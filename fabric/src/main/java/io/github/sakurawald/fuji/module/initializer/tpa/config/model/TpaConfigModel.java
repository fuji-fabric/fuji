package io.github.sakurawald.fuji.module.initializer.tpa.config.model;

import com.google.gson.annotations.SerializedName;
import io.github.sakurawald.fuji.core.annotation.Document;
import io.github.sakurawald.fuji.core.job.impl.MentionPlayersJob;

public class TpaConfigModel {

    @SerializedName(value = "request_timeout", alternate = "timeout")
    @Document("This is the timeout.")
    public int request_timeout = 300;

    public MentionPlayersJob.MentionPlayer mention_player = new MentionPlayersJob.MentionPlayer();
}
