package io.github.sakurawald.module.initializer.fuji.gui;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import eu.pb4.sgui.api.gui.SimpleGui;
import io.github.sakurawald.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.core.command.argument.adapter.abst.BaseArgumentTypeAdapter;
import io.github.sakurawald.core.gui.PagedGui;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ArgumentTypeGui extends PagedGui<BaseArgumentTypeAdapter> {

    public ArgumentTypeGui(ServerPlayerEntity player, @NotNull List<BaseArgumentTypeAdapter> entities, int pageIndex) {
        super(null, player, TextHelper.getTextByKey(player, "command.argument.type.gui.title"), entities, pageIndex);
    }

    @Override
    protected PagedGui<BaseArgumentTypeAdapter> make(@Nullable SimpleGui parent, ServerPlayerEntity player, Text title, @NotNull List<BaseArgumentTypeAdapter> entities, int pageIndex) {
        return new ArgumentTypeGui(player, entities, pageIndex);
    }

    @Override
    protected GuiElementInterface toGuiElement(BaseArgumentTypeAdapter entity) {
        return new GuiElementBuilder()
            .setName(Text.literal(entity.getClass().getSimpleName()))
            .setItem(Items.HOPPER)
            .setLore(List.of(
                TextHelper.getTextByKey(getPlayer(), "command.argument.type.class", entity.getTypeClasses().stream().map(Class::getSimpleName).toList())
                , TextHelper.getTextByKey(getPlayer(), "command.argument.type.string", entity.getTypeStrings())
            ))
            .build();
    }

    @Override
    protected List<BaseArgumentTypeAdapter> filter(String keyword) {
        return getEntities().stream()
            .filter(it -> it.getTypeClasses().stream().anyMatch(c -> c.getSimpleName().contains(keyword))
                || it.getTypeStrings().stream().anyMatch(s -> s.contains(keyword))
            )
            .toList();
    }
}
