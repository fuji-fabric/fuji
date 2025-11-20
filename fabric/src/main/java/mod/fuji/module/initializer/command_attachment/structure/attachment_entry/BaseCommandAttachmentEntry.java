package mod.fuji.module.initializer.command_attachment.structure.attachment_entry;

import com.google.gson.annotations.SerializedName;
import mod.fuji.core.auxiliary.minecraft.TextHelper;
import mod.fuji.core.gui.interfaces.LoreProvider;
import mod.fuji.module.initializer.command_attachment.command.argument.wrapper.ExecuteAsType;
import mod.fuji.module.initializer.command_attachment.command.argument.wrapper.InteractType;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings({"unused"})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BaseCommandAttachmentEntry implements LoreProvider {
    public CommandAttackmentType type = CommandAttackmentType.ITEMSTACK;
    public InteractType interactType;
    public ExecuteAsType executeAsType;
    public String command;
    public int maxUseTimes;
    public int useTimes;
    @SerializedName(value = "vanish_on_exhaust", alternate = {"destroy_item", "consume_item_on_exhaust"})
    boolean vanishOnExhaust = false;


    public void onUsed(@NotNull ServerPlayer player) {
        this.useTimes++;
    }

    @Override
    public List<Component> asLore(@NotNull ServerPlayer player) {
        return List.of(
            TextHelper.getTextByKey(player, "command_attachment.attachment.type", this.type),
            TextHelper.getTextByKey(player, "command_attachment.attachment.interact_type", this.interactType),
            TextHelper.getTextByKey(player, "command_attachment.attachment.execute_as_type", this.executeAsType),
            TextHelper.getTextByKey(player, "command_attachment.attachment.command", TextHelper.Parsers.escapeTags(this.command)),
            TextHelper.getTextByKey(player, "command_attachment.attachment.use_times", this.useTimes),
            TextHelper.getTextByKey(player, "command_attachment.attachment.max_use_times", this.maxUseTimes),
            TextHelper.getTextByKey(player, "command_attachment.attachment.vanish_on_exhaust", this.vanishOnExhaust)
        );
    }
}
