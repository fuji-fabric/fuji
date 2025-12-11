package mod.fuji.module.initializer.fuji.gui;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import eu.pb4.sgui.api.gui.SimpleGui;
import mod.fuji.core.auxiliary.minecraft.RegistryHelper;
import mod.fuji.core.auxiliary.minecraft.TextHelper;
import mod.fuji.core.gui.component.gui.PagedGui;
import mod.fuji.core.structure.IdentifierIR;
import mod.fuji.module.initializer.fuji.structure.IdentifierDescriptor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.Registry;
import net.minecraft.resources.RegistryDataLoader;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class RegistriesInspectionGui extends PagedGui<IdentifierDescriptor> {

    private final boolean isMetaRegistry;

    public RegistriesInspectionGui(@Nullable SimpleGui parent, ServerPlayer player, boolean isMetaRegistry, @NotNull List<IdentifierDescriptor> entities, int pageIndex) {
        super(parent, player, TextHelper.getTextByKey(player, "registry.list.gui.title"), entities, pageIndex);
        this.isMetaRegistry = isMetaRegistry;
    }

    public static RegistriesInspectionGui inspectAll(ServerPlayer player) {
        /* Get the identifiers of meta registries. */
        List<IdentifierIR> staticRegistries = BuiltInRegistries.REGISTRY.registryKeySet()
            .stream()
            .map(RegistryHelper::getIdentifier)
            .toList();
        List<IdentifierIR> dynamicRegistries = RegistryDataLoader.WORLDGEN_REGISTRIES
            .stream()
            .map(it -> RegistryHelper.getIdentifier(it.key()))
            .toList();

        /* Map it to descriptor. */
        List<IdentifierDescriptor> ids = new ArrayList<>();
        staticRegistries.forEach(id -> ids.add(new IdentifierDescriptor(id, false)));
        dynamicRegistries.forEach(id -> ids.add(new IdentifierDescriptor(id, true)));
        ids.sort(Comparator.comparing(it -> it.getIdentifier().getNativeValue()));

        return new RegistriesInspectionGui(null, player, true, ids, 0);
    }

    @Override
    protected @NotNull PagedGui<IdentifierDescriptor> makePage(@Nullable SimpleGui parent, @NotNull ServerPlayer player, Component title, @NotNull List<IdentifierDescriptor> entities, int pageIndex) {
        return new RegistriesInspectionGui(parent, player, this.isMetaRegistry, entities, pageIndex);
    }

    @Override
    protected @NotNull GuiElementInterface toGuiElement(@NotNull IdentifierDescriptor entity) {
        List<Component> lore = new ArrayList<>();
        lore.add(TextHelper.getTextByKey(getPlayer(), "registry.type.is_dynamic", entity.isDynamic()));
        if (this.isMetaRegistry) {
            lore.add(TextHelper.getTextByKey(getPlayer(), "prompt.click.see_inside"));
        }

        GuiElementBuilder guiElementBuilder = new GuiElementBuilder()
            .setName(Component.nullToEmpty(entity.getIdentifier().toString()))
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
            Object o = RegistryHelper.getValue(BuiltInRegistries.REGISTRY, entity.getIdentifier());
            if (o instanceof Registry<?> r) {
                List<IdentifierDescriptor> ids = r.registryKeySet()
                    .stream()
                    .map(RegistryHelper::getIdentifier)
                    .sorted()
                    .map(identifier -> new IdentifierDescriptor(identifier, false))
                    .toList();
                new RegistriesInspectionGui(getBackendGui(), getPlayer(), false, ids, 0)
                    .open();
                return;
            }

            /* try to get the registry from dynamic registries */
            Optional<RegistryDataLoader.RegistryData<?>> first = RegistryDataLoader.WORLDGEN_REGISTRIES
                .stream()
                .filter(it -> RegistryHelper.getIdAsString(it.key())
                    .equals(entity.getIdentifier().toString()))
                .findFirst();
            if (first.isPresent()) {
                List<IdentifierDescriptor> ids = RegistryHelper
                    .getRegistry(first.get().key())
                    .keySet()
                    .stream()
                    .sorted()
                    .map(IdentifierIR::of)
                    .map(identifier -> new IdentifierDescriptor(identifier, true))
                    .toList();
                new RegistriesInspectionGui(getBackendGui(), getPlayer(), false, ids, 0).open();
                return;
            }

        };
    }

}
