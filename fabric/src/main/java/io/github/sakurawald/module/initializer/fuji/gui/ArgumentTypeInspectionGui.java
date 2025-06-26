package io.github.sakurawald.module.initializer.fuji.gui;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import eu.pb4.sgui.api.gui.SimpleGui;
import io.github.sakurawald.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.core.command.argument.adapter.abst.BaseArgumentTypeAdapter;
import io.github.sakurawald.core.gui.PagedGui;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ArgumentTypeInspectionGui extends PagedGui<BaseArgumentTypeAdapter> {

    public ArgumentTypeInspectionGui(SimpleGui parent, ServerPlayerEntity player, @NotNull List<BaseArgumentTypeAdapter> entities, int pageIndex) {
        super(parent, player, TextHelper.getTextByKey(player, "command.argument.type.gui.title"), entities, pageIndex);
    }

    public static ArgumentTypeInspectionGui inspectAll(SimpleGui parent, ServerPlayerEntity player) {
        List<BaseArgumentTypeAdapter> adapters = BaseArgumentTypeAdapter.getAdapters();
        return new ArgumentTypeInspectionGui(parent, player, adapters, 0);
    }

    @Override
    protected PagedGui<BaseArgumentTypeAdapter> make(@Nullable SimpleGui parent, ServerPlayerEntity player, Text title, @NotNull List<BaseArgumentTypeAdapter> entities, int pageIndex) {
        return new ArgumentTypeInspectionGui(parent, player, entities, pageIndex);
    }

    private Item toItem(BaseArgumentTypeAdapter adapter) {
        if (isVanillaMinecraftArgumentType(adapter)) {
            return Items.HOPPER_MINECART;
        }
        return Items.HOPPER;
    }

    private static boolean isVanillaMinecraftArgumentType(BaseArgumentTypeAdapter adapter) {
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
    protected GuiElementInterface toGuiElement(BaseArgumentTypeAdapter entity) {
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

    @Override
    protected List<BaseArgumentTypeAdapter> filter(String keyword) {
        return getEntities().stream()
            .filter(it -> it.getTypeClasses().stream().anyMatch(c -> c.getSimpleName().contains(keyword))
                || it.getTypeStrings().stream().anyMatch(s -> s.contains(keyword))
                || it.getClass().getName().contains(keyword)
            )
            .toList();
    }
}
