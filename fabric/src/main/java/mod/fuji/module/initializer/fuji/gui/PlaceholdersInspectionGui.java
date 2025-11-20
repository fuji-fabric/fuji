package mod.fuji.module.initializer.fuji.gui;

import eu.pb4.sgui.api.gui.SimpleGui;
import mod.fuji.core.auxiliary.minecraft.TextHelper;
import mod.fuji.core.document.descriptor.PlaceholderDescriptor;
import mod.fuji.core.document.descriptor.StringDescriptor;
import mod.fuji.core.gui.component.gui.PagedGui;
import java.util.Comparator;
import java.util.List;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PlaceholdersInspectionGui extends StringDescriptorInspectionGui {

    public PlaceholdersInspectionGui(@Nullable SimpleGui parent, ServerPlayer player, @NotNull List<StringDescriptor> entities, int pageIndex) {
        super(parent, player, TextHelper.getTextByKey(player, "fuji.inspect.placeholders.gui.title"), entities, pageIndex);
    }

    public static PlaceholdersInspectionGui inspectAll(SimpleGui parent, ServerPlayer player) {
        List<StringDescriptor> entities = PlaceholderDescriptor.getPlaceholderDescriptors()
            .stream()
            .sorted(Comparator.comparing(StringDescriptor::getFromModule))
            .toList();

        return new PlaceholdersInspectionGui(parent, player, entities, 0);
    }

    @Override
    protected @NotNull PagedGui<StringDescriptor> makePage(@Nullable SimpleGui parent, @NotNull ServerPlayer player, Component title, @NotNull List<StringDescriptor> entities, int pageIndex) {
        return new PlaceholdersInspectionGui(parent, player, entities, pageIndex);
    }

    @Override
    protected Component toNameText(StringDescriptor entity) {
        String string = entity.toNameString();
        return TextHelper.getTextByKey(getPlayer(), "fuji.inspect.placeholders.gui.item.name", string);
    }
}
