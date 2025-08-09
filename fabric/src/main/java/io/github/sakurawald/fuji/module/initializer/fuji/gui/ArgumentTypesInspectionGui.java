package io.github.sakurawald.fuji.module.initializer.fuji.gui;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import eu.pb4.sgui.api.gui.SimpleGui;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.fuji.core.command.argument.adapter.abst.BaseArgumentTypeAdapter;
import io.github.sakurawald.fuji.core.document.annotation.DocStringProvider;
import io.github.sakurawald.fuji.core.document.auxiliary.DocumentUtil;
import io.github.sakurawald.fuji.core.gui.component.gui.PagedGui;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ArgumentTypesInspectionGui extends PagedGui<BaseArgumentTypeAdapter> {

    public ArgumentTypesInspectionGui(SimpleGui parent, ServerPlayerEntity player, @NotNull List<BaseArgumentTypeAdapter> entities, int pageIndex) {
        super(parent, player, TextHelper.getTextByKey(player, "command.argument.type.gui.title"), entities, pageIndex);
    }

    public static ArgumentTypesInspectionGui inspectAll(SimpleGui parent, ServerPlayerEntity player) {
        List<BaseArgumentTypeAdapter> adapters = BaseArgumentTypeAdapter.REGISTERED_COMMAND_ARGUMENT_TYPE_ADAPTERS;
        return new ArgumentTypesInspectionGui(parent, player, adapters, 0);
    }

    @Override
    protected PagedGui<BaseArgumentTypeAdapter> make(@Nullable SimpleGui parent, @NotNull ServerPlayerEntity player, Text title, @NotNull List<BaseArgumentTypeAdapter> entities, int pageIndex) {
        return new ArgumentTypesInspectionGui(parent, player, entities, pageIndex);
    }

    private @NotNull Item toItem(@NotNull BaseArgumentTypeAdapter entity) {
        if (entity.isVanillaMinecraftArgumentType()) {
            return Items.HOPPER_MINECART;
        }
        return Items.HOPPER;
    }

    @DocStringProvider(id = 1754726496693L, value = """
        This `argument type adapter` is used to `handle` a `vanilla` Minecraft argument type.
        A `vanilla` Minecraft argument type is a `built-in` type provided by the base Minecraft game.
        Examples include: `int`, `double`, `float`, `entity type`, `item type`, `block pos`...
        """)
    @DocStringProvider(id = 1754726568501L, value = """
        This `argument type adapter` is used to `handle` a `non-vanilla` Minecraft argument type.
        A `non-vanilla` Minecraft argument type is registered by a `module`.
        Examples include: `home name`, `warp name`, `jail name`...
        """)
    private List<Text> toDocumentTexts(@NotNull BaseArgumentTypeAdapter entity) {
        String docString;
        if (entity.isVanillaMinecraftArgumentType()) {
            docString = DocumentUtil.getDocString(getPlayer(), 1754726496693L);
        } else {
            docString = DocumentUtil.getDocString(getPlayer(), 1754726568501L);
        }

        return TextHelper.getDocumentTextList(getPlayer(), docString);
    }

    @Override
    protected @NotNull GuiElementInterface toGuiElement(@NotNull BaseArgumentTypeAdapter entity) {
        List<Text> lore = new ArrayList<>();
        lore.add(TextHelper.getTextByKey(getPlayer(), "from_module", entity.getSourceModule()));
        lore.add(TextHelper.getTextByKey(getPlayer(), "command.argument.type.class", entity.getTypeClasses().stream().map(Class::getSimpleName).toList()));
        lore.add(TextHelper.getTextByKey(getPlayer(), "command.argument.type.string", entity.getTypeStrings()));
        lore.add(TextHelper.TEXT_EMPTY);
        lore.addAll(toDocumentTexts(entity));


        GuiElementBuilder guiElementBuilder = new GuiElementBuilder()
            .setName(Text.literal(entity.getClass().getSimpleName()))
            .setItem(toItem(entity))
            .setLore(lore);

        if (!entity.isVanillaMinecraftArgumentType()) {
            guiElementBuilder.glow();
        }

        return guiElementBuilder
            .build();
    }

}
