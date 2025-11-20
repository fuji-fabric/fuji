package mod.fuji.module.initializer.command_attachment.structure.attachment_entry;

import mod.fuji.module.initializer.command_attachment.command.argument.wrapper.ExecuteAsType;
import mod.fuji.module.initializer.command_attachment.command.argument.wrapper.InteractType;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ItemStackCommandAttachmentEntry extends BaseCommandAttachmentEntry {

    public ItemStackCommandAttachmentEntry(String command, InteractType interactType, ExecuteAsType executeAsType, int maxUseTimes, int useTimes, boolean vanishOnExhaust) {
        super(CommandAttackmentType.ITEMSTACK, interactType, executeAsType, command, maxUseTimes, useTimes, vanishOnExhaust);
    }

    @Override
    public void onUsed(@NotNull ServerPlayer player) {
        super.onUsed(player);
        if (this.isVanishOnExhaust() && this.getUseTimes() >= this.getMaxUseTimes()) {
            player.getMainHandItem().shrink(1);
        }
    }

    @Override
    public List<Component> asLore(@NotNull ServerPlayer player) {
        return super.asLore(player);
    }
}
