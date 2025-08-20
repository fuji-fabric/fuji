package io.github.sakurawald.fuji.module.initializer.command_attachment.structure.attachment_entry;

import io.github.sakurawald.fuji.module.initializer.command_attachment.command.argument.wrapper.ExecuteAsType;
import io.github.sakurawald.fuji.module.initializer.command_attachment.command.argument.wrapper.InteractType;
import io.github.sakurawald.fuji.module.initializer.command_attachment.structure.CommandAttackmentType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class EntityCommandAttachmentEntry extends BaseCommandAttachmentEntry {
    public EntityCommandAttachmentEntry(String command, InteractType interactType, ExecuteAsType executeAsType, int maxUseTimes, int useTimes) {
        super(CommandAttackmentType.ENTITY, command, interactType, executeAsType, maxUseTimes, useTimes);
    }
}
