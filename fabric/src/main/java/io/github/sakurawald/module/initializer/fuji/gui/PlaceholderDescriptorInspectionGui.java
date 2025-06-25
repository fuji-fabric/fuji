package io.github.sakurawald.module.initializer.fuji.gui;

import eu.pb4.sgui.api.gui.SimpleGui;
import io.github.sakurawald.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.core.gui.PagedGui;
import io.github.sakurawald.core.structure.descriptor.StringDescriptor;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class PlaceholderDescriptorInspectionGui extends StringDescriptorInspectionGui{

    public PlaceholderDescriptorInspectionGui(@Nullable SimpleGui parent, ServerPlayerEntity player, @NotNull List<StringDescriptor> entities, int pageIndex) {
        super(parent, player, TextHelper.getTextByKey(player,"fuji.inspect.permissions_and_metas.gui.title"), entities, pageIndex);
    }

    @Override
    protected PagedGui<StringDescriptor> make(@Nullable SimpleGui parent, ServerPlayerEntity player, Text title, @NotNull List<StringDescriptor> entities, int pageIndex) {
        return new PlaceholderDescriptorInspectionGui(parent, player, entities, pageIndex);
    }

    @Override
    protected Text toNameText(StringDescriptor entity) {
        return TextHelper.getTextByKey(getPlayer(), "fuji.inspect.permissions_and_metas.gui.item.name", entity.getString());
    }
}
