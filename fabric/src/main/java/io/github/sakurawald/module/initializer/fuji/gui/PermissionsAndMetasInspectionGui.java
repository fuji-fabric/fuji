package io.github.sakurawald.module.initializer.fuji.gui;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import eu.pb4.sgui.api.gui.SimpleGui;
import io.github.sakurawald.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.core.gui.PagedGui;
import io.github.sakurawald.core.structure.descriptor.StringDescriptor;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class PermissionsAndMetasInspectionGui extends PagedGui<StringDescriptor> {

    public PermissionsAndMetasInspectionGui(@Nullable SimpleGui parent, ServerPlayerEntity player, @NotNull List<StringDescriptor> entities, int pageIndex) {
        super(parent, player, TextHelper.getTextByKey(player, "fuji.inspect.permissions_and_metas.gui.title"), entities, pageIndex);
    }

    @Override
    protected PagedGui<StringDescriptor> make(@Nullable SimpleGui parent, ServerPlayerEntity player, Text title, @NotNull List<StringDescriptor> entities, int pageIndex) {
        return new PermissionsAndMetasInspectionGui(parent, player, entities, pageIndex);
    }

    @Override
    protected GuiElementInterface toGuiElement(StringDescriptor entity) {
        GuiElementBuilder builder = new GuiElementBuilder();

        List<Text> lore = new ArrayList<>();

        /* Attach the source module. */
        lore.add(TextHelper.getTextByKey(getPlayer(), "from_module", entity.getFromModule()));

        /* Attach the type of string descriptor. */
        lore.add(TextHelper.getTextByKey(getPlayer(), "object.type", entity.getStringType()));

        /* Attach the document string. */
        List<Text> documentTextList = TextHelper.getDocumentTextList(getPlayer(), entity.getDocument());
        lore.add(TextHelper.TEXT_EMPTY);
        lore.addAll(documentTextList);

        Text nameText = TextHelper.getTextByKey(getPlayer(), "fuji.inspect.permissions_and_metas.gui.item.name", entity.getPattern());

        builder.setItem(entity.toItem())
            .setName(nameText)
            .setLore(lore);

        return builder.build();
    }

    @Override
    protected List<StringDescriptor> filter(String keyword) {
        return getEntities()
            .stream()
            .filter(it -> it.getDocument().contains(keyword)
                || it.getPattern().contains(keyword)
                || it.getFromModule().contains(keyword))
            .toList();
    }
}
