package mod.fuji.module.initializer.fuji.gui;

import eu.pb4.sgui.api.gui.SimpleGui;
import mod.fuji.core.auxiliary.minecraft.TextHelper;
import mod.fuji.core.gui.component.gui.PagedGui;
import mod.fuji.core.document.descriptor.MetaDescriptor;
import mod.fuji.core.document.descriptor.PermissionDescriptor;
import mod.fuji.core.document.descriptor.StringDescriptor;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.List;

public class PermissionsAndMetasInspectionGui extends StringDescriptorInspectionGui {

    public PermissionsAndMetasInspectionGui(@Nullable SimpleGui parent, ServerPlayer player, @NotNull List<StringDescriptor> entities, int pageIndex) {
        super(parent, player, TextHelper.getTextByKey(player, "fuji.inspect.permissions_and_metas.gui.title"), entities, pageIndex);
    }

    public static PermissionsAndMetasInspectionGui inspectAll(SimpleGui parent, ServerPlayer player) {
        List<StringDescriptor> entities = StringDescriptor.REGISTERED_STRING_DESCRIPTORS
            .stream()
            .filter(it -> it instanceof PermissionDescriptor
            || it instanceof MetaDescriptor<?>)
            .sorted(Comparator.comparing(StringDescriptor::getFromModule)
                .thenComparing(StringDescriptor::sortPriority))
            .toList();

        return new PermissionsAndMetasInspectionGui(parent, player, entities, 0);
    }

    @Override
    protected @NotNull PagedGui<StringDescriptor> makePage(@Nullable SimpleGui parent, @NotNull ServerPlayer player, Component title, @NotNull List<StringDescriptor> entities, int pageIndex) {
        return new PermissionsAndMetasInspectionGui(parent, player, entities, pageIndex);
    }

    @Override
    protected Component toNameText(StringDescriptor entity) {
        return TextHelper.getTextByKey(getPlayer(), "fuji.inspect.permissions_and_metas.gui.item.name", entity.toNameString());
    }
}
