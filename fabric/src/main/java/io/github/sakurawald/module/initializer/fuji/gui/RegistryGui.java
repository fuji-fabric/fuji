package io.github.sakurawald.module.initializer.fuji.gui;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import eu.pb4.sgui.api.gui.SimpleGui;
import io.github.sakurawald.core.auxiliary.minecraft.RegistryHelper;
import io.github.sakurawald.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.core.gui.PagedGui;
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

import java.util.List;
import java.util.Optional;

public class RegistryGui extends PagedGui<Identifier> {

    private final boolean isMetaRegistry;

    public RegistryGui(@Nullable SimpleGui parent, ServerPlayerEntity player, boolean isMetaRegistry, @NotNull List<Identifier> entities, int pageIndex) {
        super(parent, player, TextHelper.getTextByKey(player, "registry.list.gui.title"), entities, pageIndex);
        this.isMetaRegistry = isMetaRegistry;
    }

    @Override
    protected PagedGui<Identifier> make(@Nullable SimpleGui parent, ServerPlayerEntity player, Text title, @NotNull List<Identifier> entities, int pageIndex) {
        return new RegistryGui(parent, player, this.isMetaRegistry, entities, pageIndex);
    }

    @Override
    protected GuiElementInterface toGuiElement(Identifier entity) {
        return new GuiElementBuilder()
            .setName(Text.of(entity.toString()))
            .setItem(this.isMetaRegistry ? Items.BOOKSHELF : Items.PAPER)
            .setCallback(() -> {
                if (!this.isMetaRegistry) return;

                /* try to get the registry from static registries */
                Object o = Registries.REGISTRIES.get(entity);
                if (o instanceof Registry<?> r) {
                    List<Identifier> list = r.getKeys().stream()
                        .map(RegistryKey::getValue)
                        .sorted()
                        .toList();
                    new RegistryGui(getGui(), getPlayer(), false, list, 0).open();
                    return;
                }

                /* try to get the registry from dynamic registries */
                Optional<RegistryLoader.Entry<?>> first = RegistryLoader.DYNAMIC_REGISTRIES.stream().filter(it -> it.comp_985().getValue().equals(entity)).findFirst();
                if (first.isPresent()) {
                    List<Identifier> list = RegistryHelper.ofRegistry(first.get().comp_985()).getIds().stream().toList();
                    new RegistryGui(getGui(), getPlayer(), false, list, 0).open();
                    return;
                }

            })
            .build();
    }

    @Override
    protected List<Identifier> filter(String keyword) {
        return getEntities()
            .stream()
            .filter(it -> it.toString().contains(keyword))
            .toList();
    }
}
