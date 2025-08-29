package io.github.sakurawald.fuji.module.initializer.command_attachment.structure.attachment_entry;

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
public class BlockCommandAttachmentEntry extends BaseCommandAttachmentEntry {

    public BlockCommandAttachmentEntry(String created_in, String command, InteractType interactType, ExecuteAsType executeAsType, int maxUseTimes, int useTimes) {
        super(CommandAttackmentType.BLOCK, command, interactType, executeAsType, maxUseTimes, useTimes);
    }

    @Override
    public List<Text> asLore(@NotNull ServerPlayerEntity player) {
        return List.of();
    }
}
