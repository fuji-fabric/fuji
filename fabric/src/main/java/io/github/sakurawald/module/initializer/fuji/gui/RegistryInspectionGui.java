package io.github.sakurawald.module.initializer.fuji.gui;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import eu.pb4.sgui.api.gui.SimpleGui;
import io.github.sakurawald.core.auxiliary.minecraft.RegistryHelper;
import io.github.sakurawald.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.core.gui.PagedGui;
import io.github.sakurawald.module.initializer.fuji.structure.IdentifierDescriptor;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryLoader;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public class RegistryInspectionGui extends PagedGui<IdentifierDescriptor> {

    private final boolean isMetaRegistry;

    public RegistryInspectionGui(@Nullable SimpleGui parent, ServerPlayerEntity player, boolean isMetaRegistry, @NotNull List<IdentifierDescriptor> entities, int pageIndex) {
        super(parent, player, TextHelper.getTextByKey(player, "registry.list.gui.title"), entities, pageIndex);
        this.isMetaRegistry = isMetaRegistry;
    }

    @Override
    protected PagedGui<IdentifierDescriptor> make(@Nullable SimpleGui parent, ServerPlayerEntity player, Text title, @NotNull List<IdentifierDescriptor> entities, int pageIndex) {
        return new RegistryInspectionGui(parent, player, this.isMetaRegistry, entities, pageIndex);
    }

    @Override
    protected GuiElementInterface toGuiElement(IdentifierDescriptor entity) {
        GuiElementBuilder guiElementBuilder = new GuiElementBuilder()
            .setName(Text.of(entity.getIdentifier().toString()))
            .setItem(this.isMetaRegistry ? Items.BOOK : Items.PAPER)
            .setLore(List.of(
                TextHelper.getTextByKey(getPlayer(), "registry.type.is_dynamic", entity.isDynamic())
            ))
            .setCallback(openRegistry(entity));

        if (entity.isDynamic()) {
            guiElementBuilder.glow();
        }

        return guiElementBuilder
            .build();
    }

    @SuppressWarnings("UnnecessaryReturnStatement")
    private @NotNull Runnable openRegistry(IdentifierDescriptor entity) {
        return () -> {
            if (!this.isMetaRegistry) return;

            /* try to get the registry from static registries */
            Object o = Registries.REGISTRIES.get(entity.getIdentifier());
            if (o instanceof Registry<?> r) {
                List<IdentifierDescriptor> ids = r.getKeys()
                    .stream()
                    .map(RegistryKey::getValue)
                    .sorted()
                    .map(identifier -> new IdentifierDescriptor(identifier, false))
                    .toList();
                new RegistryInspectionGui(getGui(), getPlayer(), false, ids, 0)
                    .open();
                return;
            }

            /* try to get the registry from dynamic registries */
            Optional<RegistryLoader.Entry<?>> first = RegistryLoader.DYNAMIC_REGISTRIES
                .stream()
                .filter(it -> it.comp_985().getValue().equals(entity.getIdentifier()))
                .findFirst();
            if (first.isPresent()) {
                List<IdentifierDescriptor> ids = RegistryHelper
                    .ofRegistry(first.get().comp_985())
                    .getIds()
                    .stream()
                    .sorted()
                    .map(identifier -> new IdentifierDescriptor(identifier, true))
                    .toList();
                new RegistryInspectionGui(getGui(), getPlayer(), false, ids, 0).open();
                return;
            }

        };
    }

    @Override
    protected List<IdentifierDescriptor> filter(String keyword) {
        return getEntities()
            .stream()
            .filter(it -> it.getIdentifier().toString().contains(keyword))
            .toList();
    }
}
