package io.github.sakurawald.fuji.module.initializer.fuji.gui;

import eu.pb4.sgui.api.gui.SimpleGui;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.fuji.core.gui.component.gui.PagedGui;
import io.github.sakurawald.fuji.core.document.descriptor.MetaDescriptor;
import io.github.sakurawald.fuji.core.document.descriptor.PermissionDescriptor;
import io.github.sakurawald.fuji.core.document.descriptor.StringDescriptor;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.List;

public class PermissionsAndMetasInspectionGui extends StringDescriptorInspectionGui {

    public PermissionsAndMetasInspectionGui(@Nullable SimpleGui parent, ServerPlayerEntity player, @NotNull List<StringDescriptor> entities, int pageIndex) {
        super(parent, player, TextHelper.getTextByKey(player, "fuji.inspect.permissions_and_metas.gui.title"), entities, pageIndex);
    }

    public static PermissionsAndMetasInspectionGui inspectAll(SimpleGui parent, ServerPlayerEntity player) {
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
    protected PagedGui<StringDescriptor> make(@Nullable SimpleGui parent, @NotNull ServerPlayerEntity player, Text title, @NotNull List<StringDescriptor> entities, int pageIndex) {
        return new PermissionsAndMetasInspectionGui(parent, player, entities, pageIndex);
    }

    @Override
    protected Text toNameText(StringDescriptor entity) {
        return TextHelper.getTextByKey(getPlayer(), "fuji.inspect.permissions_and_metas.gui.item.name", entity.getPattern());
    }
}
