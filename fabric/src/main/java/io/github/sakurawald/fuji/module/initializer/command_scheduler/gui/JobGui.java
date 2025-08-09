package io.github.sakurawald.fuji.module.initializer.command_scheduler.gui;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import eu.pb4.sgui.api.gui.SimpleGui;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.GuiHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.fuji.core.gui.impl.gui.PagedGui;
import io.github.sakurawald.fuji.module.initializer.command_scheduler.structure.Job;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class JobGui extends PagedGui<Job> {

    public JobGui(ServerPlayerEntity player, @NotNull List<Job> entities, int pageIndex) {
        super(null, player, TextHelper.getTextByKey(player, "job.list.gui.title"), entities, pageIndex);
    }

    @Override
    protected PagedGui<Job> make(@Nullable SimpleGui parent, @NotNull ServerPlayerEntity player, Text title, @NotNull List<Job> entities, int pageIndex) {
        return new JobGui(player, entities, pageIndex);
    }

    @Override
    protected @NotNull GuiElementInterface toGuiElement(@NotNull Job entity) {
        return new GuiElementBuilder()
            .setName(Text.literal(entity.getName()))
            .setItem(GuiHelper.Material.fromBooleanValue(entity.isEnable()))
            .setLore(List.of(
                TextHelper.getTextByKey(getPlayer(), "job.props.enabled", entity.isEnable())
                , TextHelper.getTextByKey(getPlayer(), "job.props.left_times", entity.getLeftTimes())
            ))
            .build();
    }

}
