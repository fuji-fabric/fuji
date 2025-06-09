package io.github.sakurawald.module.initializer.chat.mention.config.model;

import io.github.sakurawald.core.job.impl.MentionPlayersJob;

public class ChatMentionConfigModel {

    public MentionPlayersJob.MentionPlayer mention_player = new MentionPlayersJob.MentionPlayer();

    public String mention_format = "<aqua>@%s</aqua>";

}
