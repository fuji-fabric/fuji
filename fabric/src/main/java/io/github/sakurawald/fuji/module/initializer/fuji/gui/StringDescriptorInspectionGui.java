package io.github.sakurawald.fuji.module.initializer.fuji.gui;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import eu.pb4.sgui.api.gui.SimpleGui;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.fuji.core.gui.component.gui.PagedGui;
import io.github.sakurawald.fuji.core.document.descriptor.StringDescriptor;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public abstract class StringDescriptorInspectionGui extends PagedGui<StringDescriptor> {
    public StringDescriptorInspectionGui(@Nullable SimpleGui parent, ServerPlayerEntity player, Text prefixTitle, @NotNull List<StringDescriptor> entities, int pageIndex) {
        super(parent, player, prefixTitle, entities, pageIndex);
    }

    @Override
    protected @NotNull GuiElementInterface toGuiElement(@NotNull StringDescriptor entity) {
        GuiElementBuilder builder = new GuiElementBuilder();

        List<Text> lore = new ArrayList<>();

        /* Attach the source module. */
        lore.add(TextHelper.getTextByKey(getPlayer(), "from_module", entity.getFromModule()));

        /* Attach the type of string descriptor. */
        lore.add(TextHelper.getTextByKey(getPlayer(), "object.type", entity.getStringType()));

        /* Attach the document string. */
        List<Text> documentTextList = TextHelper.getDocumentTextList(getPlayer(), entity.getDocumentString(getPlayer()));
        lore.add(TextHelper.TEXT_EMPTY);
        lore.addAll(documentTextList);

        builder.setItem(entity.toItem())
            .setName(toNameText(entity))
            .setLore(lore);

        return builder.build();
    }

    protected abstract Text toNameText(StringDescriptor entity);

}
