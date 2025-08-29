package io.github.sakurawald.fuji.module.initializer.command_attachment.structure.attachment_entry;

import io.github.sakurawald.fuji.module.initializer.command_attachment.command.argument.wrapper.ExecuteAsType;
import io.github.sakurawald.fuji.module.initializer.command_attachment.command.argument.wrapper.InteractType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ItemStackCommandAttachmentEntry extends BaseCommandAttachmentEntry {
    public boolean destroyItem;

    public ItemStackCommandAttachmentEntry(String command, InteractType interactType, ExecuteAsType executeAsType, int maxUseTimes, int useTimes, boolean destroyItem) {
        super(CommandAttackmentType.ITEMSTACK, command, interactType, executeAsType, maxUseTimes, useTimes);
        this.destroyItem = destroyItem;
    }

    @Override
    public void onUsed(@NotNull ServerPlayerEntity player) {
        super.onUsed(player);
        if (this.isDestroyItem() && this.getUseTimes() >= this.getMaxUseTimes()) {
            player.getMainHandStack().decrement(1);
        }
    }
}
