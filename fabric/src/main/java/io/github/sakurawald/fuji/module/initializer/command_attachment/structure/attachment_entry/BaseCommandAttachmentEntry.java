package io.github.sakurawald.fuji.module.initializer.command_attachment.structure.attachment_entry;

import io.github.sakurawald.fuji.module.initializer.command_attachment.command.argument.wrapper.ExecuteAsType;
import io.github.sakurawald.fuji.module.initializer.command_attachment.command.argument.wrapper.InteractType;
import io.github.sakurawald.fuji.module.initializer.command_attachment.structure.CommandAttackmentType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@SuppressWarnings({"unused"})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BaseCommandAttachmentEntry {
    public CommandAttackmentType type = CommandAttackmentType.ITEMSTACK;
    public String command;
    public InteractType interactType;
    public ExecuteAsType executeAsType;
    public int maxUseTimes;
    public int useTimes;
}
