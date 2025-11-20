package mod.fuji.module.initializer.skin.gui;

import com.mojang.authlib.properties.Property;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import eu.pb4.sgui.api.gui.SimpleGui;
import mod.fuji.core.auxiliary.minecraft.AuthlibHelper;
import mod.fuji.core.auxiliary.minecraft.TextHelper;
import mod.fuji.core.gui.component.gui.PagedGui;
import mod.fuji.module.initializer.skin.service.SkinService;
import mod.fuji.module.initializer.skin.structure.SkinDescriptor;
import java.util.List;
import net.minecraft.world.item.Items;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SkinGui extends PagedGui<SkinDescriptor> {
    public SkinGui(@Nullable SimpleGui parent, @NotNull ServerPlayer player, @NotNull List<SkinDescriptor> entities, int pageIndex) {
        super(parent, player, TextHelper.getTextByKey(player, "skin.gui.title"), entities, pageIndex);
    }

    @Override
    protected @NotNull PagedGui<SkinDescriptor> makePage(@Nullable SimpleGui parent, @NotNull ServerPlayer player, Component title, @NotNull List<SkinDescriptor> entities, int pageIndex) {
        return new SkinGui(parent, player, entities, pageIndex);
    }

    public static SkinGui makeInstance(@NotNull ServerPlayer player) {
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
