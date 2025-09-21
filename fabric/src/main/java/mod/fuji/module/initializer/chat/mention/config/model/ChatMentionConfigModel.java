package mod.fuji.module.initializer.chat.mention.config.model;

import mod.fuji.core.document.annotation.Document;
import mod.fuji.core.job.impl.PlaySoundJob;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ChatMentionConfigModel {

    PlaySoundJob.PlaySoundJobSetup mentionPlayer = new PlaySoundJob.PlaySoundJobSetup();

    @Document(id = 1751826735560L, value = """
        The format used in `chat message` when a player is `mentioned`.
        """)
    String mentionFormat = "<aqua>@%s</aqua>";

}
