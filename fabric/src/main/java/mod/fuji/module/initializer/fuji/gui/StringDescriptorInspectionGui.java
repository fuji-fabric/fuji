package mod.fuji.module.initializer.fuji.gui;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import eu.pb4.sgui.api.gui.SimpleGui;
import mod.fuji.core.auxiliary.minecraft.TextHelper;
import mod.fuji.core.gui.component.gui.PagedGui;
import mod.fuji.core.document.descriptor.StringDescriptor;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public abstract class StringDescriptorInspectionGui extends PagedGui<StringDescriptor> {
    public StringDescriptorInspectionGui(@Nullable SimpleGui parent, ServerPlayer player, Component prefixTitle, @NotNull List<StringDescriptor> entities, int pageIndex) {
        super(parent, player, prefixTitle, entities, pageIndex);
    }

    @Override
    protected @NotNull GuiElementInterface toGuiElement(@NotNull StringDescriptor entity) {
        GuiElementBuilder builder = new GuiElementBuilder();

        List<Component> lore = new ArrayList<>();

        /* Attach the source module. */
        lore.add(TextHelper.getTextByKey(getPlayer(), "from_module", entity.getFromModule()));

        /* Attach the type of string descriptor. */
        lore.add(TextHelper.getTextByKey(getPlayer(), "object.type", entity.getStringType()));

        /* Attach the document string. */
        List<Component> documentTextList = TextHelper.getDocumentTextList(getPlayer(), entity.getDocumentString(getPlayer()));
        lore.add(TextHelper.TEXT_EMPTY);
        lore.addAll(documentTextList);

        builder.setItem(entity.toItem())
            .setName(toNameText(entity))
            .setLore(lore);

        return builder.build();
    }

    protected abstract Component toNameText(StringDescriptor entity);

}
