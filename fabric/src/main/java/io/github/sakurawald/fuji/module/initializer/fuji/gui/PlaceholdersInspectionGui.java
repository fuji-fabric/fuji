package io.github.sakurawald.fuji.module.initializer.fuji.gui;

import eu.pb4.sgui.api.gui.SimpleGui;
import io.github.sakurawald.fuji.Fuji;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.fuji.core.gui.impl.gui.PagedGui;
import io.github.sakurawald.fuji.core.document.descriptor.PlaceholderDescriptor;
import io.github.sakurawald.fuji.core.document.descriptor.StringDescriptor;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.List;

public class PlaceholdersInspectionGui extends StringDescriptorInspectionGui{

    public PlaceholdersInspectionGui(@Nullable SimpleGui parent, ServerPlayerEntity player, @NotNull List<StringDescriptor> entities, int pageIndex) {
        super(parent, player, TextHelper.getTextByKey(player,"fuji.inspect.placeholders.gui.title"), entities, pageIndex);
    }

    public static PlaceholdersInspectionGui inspectAll(SimpleGui parent, ServerPlayerEntity player) {
        List<StringDescriptor> entities = StringDescriptor.REGISTERED_STRING_DESCRIPTORS
            .stream()
            .filter(it -> it instanceof PlaceholderDescriptor)
            .sorted(Comparator.comparing(StringDescriptor::getFromModule))
            .toList();

        return new PlaceholdersInspectionGui(parent, player, entities,0);
    }

    @Override
    protected PagedGui<StringDescriptor> make(@Nullable SimpleGui parent, @NotNull ServerPlayerEntity player, Text title, @NotNull List<StringDescriptor> entities, int pageIndex) {
        return new PlaceholdersInspectionGui(parent, player, entities, pageIndex);
    }

    @Override
    protected Text toNameText(StringDescriptor entity) {
        String string = Fuji.MOD_ID + ":" + entity.getString();
        return TextHelper.getTextByKey(getPlayer(), "fuji.inspect.placeholders.gui.item.name", string);
    }
}
