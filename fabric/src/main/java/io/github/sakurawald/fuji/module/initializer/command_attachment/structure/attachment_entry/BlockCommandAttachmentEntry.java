package io.github.sakurawald.fuji.module.initializer.command_attachment.structure.attachment_entry;

import io.github.sakurawald.fuji.module.initializer.command_attachment.command.argument.wrapper.ExecuteAsType;
import io.github.sakurawald.fuji.module.initializer.command_attachment.command.argument.wrapper.InteractType;
import io.github.sakurawald.fuji.module.initializer.command_attachment.structure.CommandAttackmentType;

public class BlockCommandAttachmentEntry extends BaseCommandAttachmentEntry {

    public BlockCommandAttachmentEntry(String created_in, String command, InteractType interactType, ExecuteAsType executeAsType, int maxUseTimes, int useTimes) {
        super(CommandAttackmentType.BLOCK, command, interactType, executeAsType, maxUseTimes, useTimes);
    }
}
