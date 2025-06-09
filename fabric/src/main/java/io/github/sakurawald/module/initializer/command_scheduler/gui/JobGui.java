package io.github.sakurawald.module.initializer.command_scheduler.gui;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import eu.pb4.sgui.api.gui.SimpleGui;
import io.github.sakurawald.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.core.gui.PagedGui;
import io.github.sakurawald.module.initializer.command_scheduler.structure.Job;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
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
    protected PagedGui<Job> make(@Nullable SimpleGui parent, ServerPlayerEntity player, Text title, @NotNull List<Job> entities, int pageIndex) {
        return new JobGui(player, entities, pageIndex);
    }

    @Override
    protected GuiElementInterface toGuiElement(Job entity) {
        return new GuiElementBuilder()
            .setName(Text.literal(entity.getName()))
            .setItem(this.computeItem(entity))
            .setLore(List.of(
                TextHelper.getTextByKey(getPlayer(), "job.props.enabled", entity.isEnable())
                , TextHelper.getTextByKey(getPlayer(), "job.props.left_times", entity.getLeftTimes())
            ))
            .build();
    }

    private Item computeItem(Job entity) {
        return entity.isEnable() ? Items.GREEN_STAINED_GLASS : Items.RED_STAINED_GLASS;
    }

    @Override
    protected List<Job> filter(String keyword) {
        return getEntities().stream()
            .filter(it -> it.getName().equals(keyword))
            .toList();
    }
}
