package io.github.sakurawald.fuji.module.initializer.command_attachment.config.model;

import io.github.sakurawald.fuji.module.initializer.command_attachment.structure.attachment_entry.BaseCommandAttachmentEntry;
import java.util.concurrent.CopyOnWriteArrayList;
import lombok.Data;

@Data
public class CommandAttachmentModel {
    final CopyOnWriteArrayList<BaseCommandAttachmentEntry> entries = new CopyOnWriteArrayList<>();
}
