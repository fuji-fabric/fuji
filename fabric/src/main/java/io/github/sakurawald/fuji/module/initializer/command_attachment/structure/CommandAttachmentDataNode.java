package io.github.sakurawald.fuji.module.initializer.command_attachment.structure;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CommandAttachmentDataNode {

    String id;
    CommandAttachments model = new CommandAttachments();
}
