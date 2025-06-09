package io.github.sakurawald.module.initializer.command_attachment.structure;

import io.github.sakurawald.module.initializer.command_attachment.command.argument.wrapper.ExecuteAsType;
import io.github.sakurawald.module.initializer.command_attachment.command.argument.wrapper.InteractType;

public class EntityCommandAttachmentNode extends CommandAttachmentNode {
    public EntityCommandAttachmentNode(String command, InteractType interactType, ExecuteAsType executeAsType, int maxUseTimes, int useTimes) {
        super(CommandAttackmentType.ENTITY, command, interactType, executeAsType, maxUseTimes, useTimes);
    }
}
