package io.github.sakurawald.module.initializer.command_attachment.config.model;

import io.github.sakurawald.module.initializer.command_attachment.structure.CommandAttachmentNode;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class CommandAttachmentModel {
    final List<CommandAttachmentNode> entries = new ArrayList<>();
}
