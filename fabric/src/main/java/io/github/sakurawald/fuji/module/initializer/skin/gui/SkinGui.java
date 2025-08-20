package io.github.sakurawald.fuji.module.initializer.skin.gui;

import com.mojang.authlib.properties.Property;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import eu.pb4.sgui.api.gui.SimpleGui;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.AuthlibHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.fuji.core.gui.component.gui.PagedGui;
import io.github.sakurawald.fuji.module.initializer.skin.service.SkinService;
import io.github.sakurawald.fuji.module.initializer.skin.structure.SkinDescriptor;
import java.util.List;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SkinGui extends PagedGui<SkinDescriptor> {
    public SkinGui(@Nullable SimpleGui parent, @NotNull ServerPlayerEntity player, @NotNull List<SkinDescriptor> entities, int pageIndex) {
        super(parent, player, TextHelper.getTextByKey(player, "skin.gui.title"), entities, pageIndex);
    }

    @Override
    protected PagedGui<SkinDescriptor> make(@Nullable SimpleGui parent, @NotNull ServerPlayerEntity player, Text title, @NotNull List<SkinDescriptor> entities, int pageIndex) {
        return new SkinGui(parent, player, entities, pageIndex);
    }

    public static SkinGui makeInstance(@NotNull ServerPlayerEntity player) {
        List<SkinDescriptor> entities = SkinService.getDefaultSkinList();
        return new SkinGui(null, player, entities, 0);
    }

    @Override
    protected @NotNull GuiElementInterface toGuiElement(@NotNull SkinDescriptor entity) {
        GuiElementBuilder builder = new GuiElementBuilder();
        Property skinProperty = entity.getSkinProperty().toVanillaType();
        String value = AuthlibHelper.getPropertyValue(skinProperty);
        builder
            .setItem(Items.PLAYER_HEAD)
            .setName(TextHelper.getTextByKey(getPlayer(), "skin.gui.entity.name", entity.getSkinName()))
            .setLore(List.of(
                TextHelper.getTextByKey(getPlayer(), "prompt.click.apply_it")
            ))
            .setSkullOwner(value)
            .setCallback(() -> {
                closeWithoutOpenParentGui();
                SkinService.changeSkin(getPlayer(), () -> entity.getSkinProperty().toVanillaType());
            });

        return builder.build();
    }
}
