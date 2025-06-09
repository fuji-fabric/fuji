package io.github.sakurawald.module.initializer.command_attachment.structure;

import io.github.sakurawald.module.initializer.command_attachment.command.argument.wrapper.ExecuteAsType;
import io.github.sakurawald.module.initializer.command_attachment.command.argument.wrapper.InteractType;
import lombok.AllArgsConstructor;
import lombok.Data;

@SuppressWarnings({"UnusedAssignment", "unused"})
@Data
@AllArgsConstructor
public class CommandAttachmentNode {
    public CommandAttackmentType type = CommandAttackmentType.ITEMSTACK;
    public String command;
    public InteractType interactType;
    public ExecuteAsType executeAsType;
    public int maxUseTimes;
    public int useTimes;
}
