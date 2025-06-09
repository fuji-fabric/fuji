package io.github.sakurawald.module.initializer.tpa.config.model;

import io.github.sakurawald.core.job.impl.MentionPlayersJob;

public class TpaConfigModel {
    public int timeout = 300;
    public MentionPlayersJob.MentionPlayer mention_player = new MentionPlayersJob.MentionPlayer();
}
