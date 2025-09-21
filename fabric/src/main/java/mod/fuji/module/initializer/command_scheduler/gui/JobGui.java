package mod.fuji.module.initializer.command_scheduler.gui;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import eu.pb4.sgui.api.gui.SimpleGui;
import mod.fuji.core.auxiliary.minecraft.GuiHelper;
import mod.fuji.core.auxiliary.minecraft.TextHelper;
import mod.fuji.core.gui.component.gui.PagedGui;
import mod.fuji.module.initializer.command_scheduler.structure.CommandSchedulerJobDescriptor;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class JobGui extends PagedGui<CommandSchedulerJobDescriptor> {

    public JobGui(ServerPlayerEntity player, @NotNull List<CommandSchedulerJobDescriptor> entities, int pageIndex) {
        super(null, player, TextHelper.getTextByKey(player, "job.list.gui.title"), entities, pageIndex);
    }

    @Override
    protected PagedGui<CommandSchedulerJobDescriptor> makePage(@Nullable SimpleGui parent, @NotNull ServerPlayerEntity player, Text title, @NotNull List<CommandSchedulerJobDescriptor> entities, int pageIndex) {
        return new JobGui(player, entities, pageIndex);
    }

    @Override
    protected @NotNull GuiElementInterface toGuiElement(@NotNull CommandSchedulerJobDescriptor entity) {
        return new GuiElementBuilder()
            .setName(Text.literal(entity.getName()))
            .setItem(GuiHelper.Material.fromBooleanValue(entity.isEnable()))
            .setLore(List.of(
                TextHelper.getTextByKey(getPlayer(), "job.props.enabled", entity.isEnable())
                , TextHelper.getTextByKey(getPlayer(), "job.props.left_times", entity.getRemainingRuns())
            ))
            .build();
    }

}
