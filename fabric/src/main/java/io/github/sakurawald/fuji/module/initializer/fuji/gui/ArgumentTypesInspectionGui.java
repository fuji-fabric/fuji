package io.github.sakurawald.fuji.module.initializer.fuji.gui;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import eu.pb4.sgui.api.gui.SimpleGui;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.fuji.core.command.argument.adapter.abst.BaseArgumentTypeAdapter;
import io.github.sakurawald.fuji.core.gui.impl.gui.PagedGui;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ArgumentTypesInspectionGui extends PagedGui<BaseArgumentTypeAdapter> {

    public ArgumentTypesInspectionGui(SimpleGui parent, ServerPlayerEntity player, @NotNull List<BaseArgumentTypeAdapter> entities, int pageIndex) {
        super(parent, player, TextHelper.getTextByKey(player, "command.argument.type.gui.title"), entities, pageIndex);
    }

    public static ArgumentTypesInspectionGui inspectAll(SimpleGui parent, ServerPlayerEntity player) {
        List<BaseArgumentTypeAdapter> adapters = BaseArgumentTypeAdapter.REGISTERED_COMMAND_ARGUMENT_TYPE_ADAPTERS;
        return new ArgumentTypesInspectionGui(parent, player, adapters, 0);
    }

    @Override
    protected PagedGui<BaseArgumentTypeAdapter> make(@Nullable SimpleGui parent, @NotNull ServerPlayerEntity player, Text title, @NotNull List<BaseArgumentTypeAdapter> entities, int pageIndex) {
        return new ArgumentTypesInspectionGui(parent, player, entities, pageIndex);
    }

    private @NotNull Item toItem(@NotNull BaseArgumentTypeAdapter adapter) {
        if (isVanillaMinecraftArgumentType(adapter)) {
            return Items.HOPPER_MINECART;
        }
        return Items.HOPPER;
    }

    private static boolean isVanillaMinecraftArgumentType(BaseArgumentTypeAdapter adapter) {
        if (adapter.markAsVanillaMinecraftArgumentType()) {
            return true;
        }

        return adapter.getTypeClasses()
            .stream()
            .anyMatch(argumentClass -> {
                String className = argumentClass.getName();
                return className.startsWith("net.minecraft")
                    || className.startsWith("com.mojang")
                    || className.startsWith("java.lang");
            });
    }

    @Override
    protected @NotNull GuiElementInterface toGuiElement(@NotNull BaseArgumentTypeAdapter entity) {
        GuiElementBuilder guiElementBuilder = new GuiElementBuilder()
            .setName(Text.literal(entity.getClass().getSimpleName()))
            .setItem(toItem(entity))
            .setLore(List.of(
                TextHelper.getTextByKey(getPlayer(), "from_module", entity.getFromModule())
                , TextHelper.getTextByKey(getPlayer(), "command.argument.type.class", entity.getTypeClasses().stream().map(Class::getSimpleName).toList())
                , TextHelper.getTextByKey(getPlayer(), "command.argument.type.string", entity.getTypeStrings())
            ));

        if (!isVanillaMinecraftArgumentType(entity)) {
            guiElementBuilder.glow();
        }

        return guiElementBuilder
            .build();
    }

}
