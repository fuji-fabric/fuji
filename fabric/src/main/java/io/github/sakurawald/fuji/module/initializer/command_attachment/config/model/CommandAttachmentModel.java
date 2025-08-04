package io.github.sakurawald.fuji.module.initializer.command_attachment.config.model;

import io.github.sakurawald.fuji.module.initializer.command_attachment.structure.attachment_entry.BaseCommandAttachmentEntry;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class CommandAttachmentModel {
    final List<BaseCommandAttachmentEntry> entries = new ArrayList<>();
}
