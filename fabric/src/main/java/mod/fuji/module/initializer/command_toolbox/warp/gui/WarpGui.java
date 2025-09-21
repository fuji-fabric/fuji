package mod.fuji.module.initializer.command_toolbox.warp.gui;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import eu.pb4.sgui.api.gui.SimpleGui;
import mod.fuji.core.auxiliary.minecraft.ItemStackHelper;
import mod.fuji.core.auxiliary.minecraft.TextHelper;
import mod.fuji.core.gui.component.gui.PagedGui;
import mod.fuji.module.initializer.command_toolbox.warp.service.WarpService;
import mod.fuji.module.initializer.command_toolbox.warp.structure.WarpDescriptor;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class WarpGui extends PagedGui<WarpDescriptor> {

    public WarpGui(ServerPlayerEntity player, @NotNull List<WarpDescriptor> entities, int pageIndex) {
        super(null, player, TextHelper.getTextByKey(player, "warp.gui.title"), entities, pageIndex);
    }

    public static @NotNull WarpGui makeDefault(@NotNull ServerPlayerEntity player) {
        List<WarpDescriptor> list = WarpService.listWarps();
        return new WarpGui(player, list, 0);
    }

    @Override
    protected PagedGui<WarpDescriptor> makePage(@Nullable SimpleGui parent, @NotNull ServerPlayerEntity player, Text title, @NotNull List<WarpDescriptor> entities, int pageIndex) {
        return new WarpGui(player, entities, pageIndex);
    }

    @Override
    protected @NotNull GuiElementInterface toGuiElement(@NotNull WarpDescriptor entity) {
        List<Text> lore = entity
            .getLore()
            .stream()
            .map(line -> TextHelper.getTextByValue(getPlayer(), line))
            .toList();

        return GuiElementBuilder
            .from(ItemStackHelper.Parser.parseItemStack(entity.getItem()))
            .setName(TextHelper.getTextByValue(getPlayer(), entity.getDisplayName()))
            .setLore(lore)
            .setCallback(() -> {
                WarpService.doWarp(entity, getPlayer());
                close();
            })
            .build();
    }

}
