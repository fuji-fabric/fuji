package io.github.sakurawald.module.initializer.chat.mention.config.model;

import io.github.sakurawald.core.annotation.Document;
import io.github.sakurawald.core.job.impl.MentionPlayersJob;

public class ChatMentionConfigModel {

    public MentionPlayersJob.MentionPlayer mention_player = new MentionPlayersJob.MentionPlayer();

    @Document("""
        The format used in `chat message` when a player is `mentioned`.
        """)
    public String mention_format = "<aqua>@%s</aqua>";

}
