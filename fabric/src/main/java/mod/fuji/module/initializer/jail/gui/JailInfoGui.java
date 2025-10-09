package mod.fuji.module.initializer.jail.gui;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import eu.pb4.sgui.api.gui.SimpleGui;
import mod.fuji.core.auxiliary.minecraft.GuiHelper;
import mod.fuji.core.auxiliary.minecraft.TextHelper;
import mod.fuji.core.gui.component.gui.PagedGui;
import mod.fuji.module.initializer.jail.service.JailService;
import mod.fuji.module.initializer.jail.structure.JailDescriptor;
import mod.fuji.module.initializer.jail.structure.JailRecord;
import java.util.Comparator;
import java.util.List;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class JailInfoGui extends PagedGui<JailRecord> {

    private final @NotNull JailDescriptor jailDescriptor;

    public JailInfoGui(@Nullable SimpleGui parent, @NotNull ServerPlayerEntity player, @NotNull JailDescriptor jailDescriptor, @NotNull List<JailRecord> entities, int pageIndex) {
        super(parent, player, TextHelper.getTextByKey(player, "jail.list.details.gui.title", jailDescriptor.getId()), entities, pageIndex);
        this.jailDescriptor = jailDescriptor;
    }

    @Override
    protected @NotNull PagedGui<JailRecord> makePage(@Nullable SimpleGui parent, @NotNull ServerPlayerEntity player, Text title, @NotNull List<JailRecord> entities, int pageIndex) {
        return new JailInfoGui(parent, player, this.jailDescriptor, entities, pageIndex);
    }

    public static JailInfoGui make(@NotNull SimpleGui parent, @NotNull ServerPlayerEntity player, @NotNull JailDescriptor jailDescriptor) {
        List<JailRecord> jailRecords = JailService.getJailRecords(jailDescriptor);
        jailRecords.sort(Comparator
            .comparing(JailRecord::isEnable)
            .reversed());
        return new JailInfoGui(parent, player, jailDescriptor, jailRecords, 0);
    }

    @Override
    protected @NotNull GuiElementInterface toGuiElement(@NotNull JailRecord entity) {
        GuiElementBuilder builder = new GuiElementBuilder();

        ServerPlayerEntity player = getPlayer();
        builder
            .setItem(GuiHelper.Material.fromBooleanValue(entity.isEnable()))
            .setName(TextHelper.getTextByKey(player, "player.name", entity.getPrisonerName()))
            .setLore(List.of(
                TextHelper.getTextByKey(player, "jail.record.enable", entity.isEnable())
                , TextHelper.getTextByKey(player, "jail.record.prisoner_name", entity.getPrisonerName())
                , TextHelper.getTextByKey(player, "jail.record.creator_name", entity.getCreatorName())
                , TextHelper.getTextByKey(player, "jail.record.created_time", entity.getFormattedCreatedTimestamp())
                , TextHelper.getTextByKey(player, "jail.record.jail_id", this.jailDescriptor.getId())
                , TextHelper.getTextByKey(player, "jail.record.specified_jail_duration", entity.getSpecifiedJailDuration())
                , TextHelper.getTextByKey(player, "jail.record.remaining_jail_duration", entity.getRemainingJailDuration())
                , TextHelper.getTextByKey(player, "jail.record.reason", TextHelper.Parsers.escapeTags(entity.getReason()))
            ));

        return builder.build();
    }
}
