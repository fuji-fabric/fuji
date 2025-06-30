package io.github.sakurawald.fuji.module.initializer.chat.mention.config.model;

import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.core.job.impl.PlaySoundJob;

public class ChatMentionConfigModel {

    public PlaySoundJob.PlayerSoundJobSetup mention_player = new PlaySoundJob.PlayerSoundJobSetup();

    @Document("""
        The format used in `chat message` when a player is `mentioned`.
        """)
    public String mention_format = "<aqua>@%s</aqua>";

}
