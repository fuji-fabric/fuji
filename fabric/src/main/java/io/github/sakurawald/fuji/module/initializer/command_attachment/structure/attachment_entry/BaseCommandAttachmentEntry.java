package io.github.sakurawald.fuji.module.initializer.command_attachment.structure.attachment_entry;

import io.github.sakurawald.fuji.core.gui.interfaces.LoreProvider;
import io.github.sakurawald.fuji.module.initializer.command_attachment.command.argument.wrapper.ExecuteAsType;
import io.github.sakurawald.fuji.module.initializer.command_attachment.command.argument.wrapper.InteractType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings({"unused"})
@Data
@NoArgsConstructor
@AllArgsConstructor
public abstract class BaseCommandAttachmentEntry implements LoreProvider {
    public CommandAttackmentType type = CommandAttackmentType.ITEMSTACK;
    public String command;
    public InteractType interactType;
    public ExecuteAsType executeAsType;
    public int maxUseTimes;
    public int useTimes;

    public void onUsed(@NotNull ServerPlayerEntity player) {
        this.useTimes++;
    }
}
