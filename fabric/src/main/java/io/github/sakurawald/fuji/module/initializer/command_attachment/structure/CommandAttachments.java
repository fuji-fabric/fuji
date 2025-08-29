package io.github.sakurawald.fuji.module.initializer.command_attachment.structure;

import io.github.sakurawald.fuji.module.initializer.command_attachment.structure.attachment_entry.BaseCommandAttachmentEntry;
import java.util.concurrent.CopyOnWriteArrayList;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CommandAttachments {
    final CopyOnWriteArrayList<BaseCommandAttachmentEntry> entries = new CopyOnWriteArrayList<>();
}
