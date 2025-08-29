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
public class EntityCommandAttachmentEntry extends BaseCommandAttachmentEntry {
    public EntityCommandAttachmentEntry(String command, InteractType interactType, ExecuteAsType executeAsType, int maxUseTimes, int useTimes) {
        super(CommandAttackmentType.ENTITY, command, interactType, executeAsType, maxUseTimes, useTimes);
    }

    @Override
    public List<Text> asLore(@NotNull ServerPlayerEntity player) {
        return List.of();
    }
}
