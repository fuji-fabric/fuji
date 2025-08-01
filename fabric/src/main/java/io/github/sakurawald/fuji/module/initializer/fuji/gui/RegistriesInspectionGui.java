package io.github.sakurawald.fuji.module.initializer.fuji.gui;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import eu.pb4.sgui.api.gui.SimpleGui;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.RegistryHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.fuji.core.gui.impl.gui.PagedGui;
import io.github.sakurawald.fuji.module.initializer.fuji.structure.IdentifierDescriptor;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryLoader;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class RegistriesInspectionGui extends PagedGui<IdentifierDescriptor> {

    private final boolean isMetaRegistry;

    public RegistriesInspectionGui(@Nullable SimpleGui parent, ServerPlayerEntity player, boolean isMetaRegistry, @NotNull List<IdentifierDescriptor> entities, int pageIndex) {
        super(parent, player, TextHelper.getTextByKey(player, "registry.list.gui.title"), entities, pageIndex);
        this.isMetaRegistry = isMetaRegistry;
    }

    public static RegistriesInspectionGui inspectAll(ServerPlayerEntity player) {
        /* Get the identifiers of meta registries. */
        List<Identifier> staticRegistries = Registries.REGISTRIES.getKeys()
            .stream()
            .map(RegistryKey::getValue)
            .toList();
        List<Identifier> dynamicRegistries = RegistryLoader.DYNAMIC_REGISTRIES
            .stream()
            .map(it -> it.comp_985().getValue())
            .toList();

        /* Map it to descriptor. */
        List<IdentifierDescriptor> ids = new ArrayList<>();
        staticRegistries.forEach(id -> ids.add(new IdentifierDescriptor(id, false)));
        dynamicRegistries.forEach(id -> ids.add(new IdentifierDescriptor(id, true)));
        ids.sort(Comparator.comparing(IdentifierDescriptor::getIdentifier));

        return new RegistriesInspectionGui(null, player, true, ids, 0);
    }

    @Override
    protected PagedGui<IdentifierDescriptor> make(@Nullable SimpleGui parent, ServerPlayerEntity player, Text title, @NotNull List<IdentifierDescriptor> entities, int pageIndex) {
        return new RegistriesInspectionGui(parent, player, this.isMetaRegistry, entities, pageIndex);
    }

    @Override
    protected GuiElementInterface toGuiElement(IdentifierDescriptor entity) {
        List<Text> lore = new ArrayList<>();
        lore.add(TextHelper.getTextByKey(getPlayer(), "registry.type.is_dynamic", entity.isDynamic()));
        if (this.isMetaRegistry) {
            lore.add(TextHelper.getTextByKey(getPlayer(), "prompt.click.see_inside"));
        }

        GuiElementBuilder guiElementBuilder = new GuiElementBuilder()
            .setName(Text.of(entity.getIdentifier().toString()))
            .setItem(getItem(entity))
            .setLore(lore)
            .setCallback(openRegistry(entity));

        if (entity.isDynamic()) {
            guiElementBuilder.glow();
        }

        return guiElementBuilder
            .build();
    }

    private Item getItem(IdentifierDescriptor entity) {
        if (!this.isMetaRegistry) return Items.PAPER;

        return entity.isDynamic() ? Items.WRITABLE_BOOK : Items.BOOK;
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
                new RegistriesInspectionGui(getBackendGui(), getPlayer(), false, ids, 0)
                    .open();
                return;
            }

            /* try to get the registry from dynamic registries */
            Optional<RegistryLoader.Entry<?>> first = RegistryLoader.DYNAMIC_REGISTRIES
                .stream()
                .filter(it -> RegistryHelper.toString(it.comp_985())
                    .equals(entity.getIdentifier().toString()))
                .findFirst();
            if (first.isPresent()) {
                List<IdentifierDescriptor> ids = RegistryHelper
                    .getRegistry(first.get().comp_985())
                    .getIds()
                    .stream()
                    .sorted()
                    .map(identifier -> new IdentifierDescriptor(identifier, true))
                    .toList();
                new RegistriesInspectionGui(getBackendGui(), getPlayer(), false, ids, 0).open();
                return;
            }

        };
    }

}
