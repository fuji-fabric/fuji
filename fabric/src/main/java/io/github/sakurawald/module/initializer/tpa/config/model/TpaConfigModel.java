package io.github.sakurawald.module.initializer.tpa.config.model;

import com.google.gson.annotations.SerializedName;
import io.github.sakurawald.core.job.impl.MentionPlayersJob;

public class TpaConfigModel {

    @SerializedName(value = "request_timeout", alternate = "timeout")
    public int request_timeout = 300;

    public MentionPlayersJob.MentionPlayer mention_player = new MentionPlayersJob.MentionPlayer();
}
