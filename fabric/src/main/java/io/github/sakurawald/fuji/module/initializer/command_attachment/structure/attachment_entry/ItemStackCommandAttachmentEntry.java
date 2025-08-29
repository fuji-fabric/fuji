package io.github.sakurawald.fuji.module.initializer.command_attachment.structure.attachment_entry;

import com.google.gson.annotations.SerializedName;
import io.github.sakurawald.fuji.module.initializer.command_attachment.command.argument.wrapper.ExecuteAsType;
import io.github.sakurawald.fuji.module.initializer.command_attachment.command.argument.wrapper.InteractType;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ItemStackCommandAttachmentEntry extends BaseCommandAttachmentEntry {

    @SerializedName(value = "consume_item_on_exhaust", alternate = "destroy_item")
    boolean consumeItemOnExhaust;

    public ItemStackCommandAttachmentEntry(String command, InteractType interactType, ExecuteAsType executeAsType, int maxUseTimes, int useTimes, boolean consumeItemOnExhaust) {
        super(CommandAttackmentType.ITEMSTACK, command, interactType, executeAsType, maxUseTimes, useTimes);
        this.consumeItemOnExhaust = consumeItemOnExhaust;
    }

    @Override
    public void onUsed(@NotNull ServerPlayerEntity player) {
        super.onUsed(player);
        if (this.isConsumeItemOnExhaust() && this.getUseTimes() >= this.getMaxUseTimes()) {
            player.getMainHandStack().decrement(1);
        }
    }

    @Override
    public List<Text> asLore(@NotNull ServerPlayerEntity player) {
        return List.of();
    }
}
