package mod.fuji.module.initializer.fuji.gui;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import eu.pb4.sgui.api.gui.SimpleGui;
import mod.fuji.core.auxiliary.minecraft.TextHelper;
import mod.fuji.core.command.argument.adapter.abst.BaseArgumentTypeAdapter;
import mod.fuji.core.document.annotation.DocStringProvider;
import mod.fuji.core.document.auxiliary.DocumentUtil;
import mod.fuji.core.gui.component.gui.PagedGui;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ArgumentTypesInspectionGui extends PagedGui<BaseArgumentTypeAdapter> {

    public ArgumentTypesInspectionGui(SimpleGui parent, ServerPlayer player, @NotNull List<BaseArgumentTypeAdapter> entities, int pageIndex) {
        super(parent, player, TextHelper.getTextByKey(player, "command.argument.type.gui.title"), entities, pageIndex);
    }

    public static ArgumentTypesInspectionGui inspectAll(SimpleGui parent, ServerPlayer player) {
        List<BaseArgumentTypeAdapter> adapters = BaseArgumentTypeAdapter.Registry.REGISTERED_COMMAND_ARGUMENT_TYPE_ADAPTERS;
        return new ArgumentTypesInspectionGui(parent, player, adapters, 0);
    }

    @Override
    protected @NotNull PagedGui<BaseArgumentTypeAdapter> makePage(@Nullable SimpleGui parent, @NotNull ServerPlayer player, Component title, @NotNull List<BaseArgumentTypeAdapter> entities, int pageIndex) {
        return new ArgumentTypesInspectionGui(parent, player, entities, pageIndex);
    }

    private @NotNull Item toItem(@NotNull BaseArgumentTypeAdapter entity) {
        if (entity.isVanillaMinecraftArgumentType()) {
            return Items.HOPPER_MINECART;
        }
        return Items.HOPPER;
    }

    @DocStringProvider(id = 1754726496693L, value = """
        This `argument type adapter` is used to `handle` a `vanilla` Minecraft argument type.
        A `vanilla` Minecraft argument type is a `built-in` type provided by the base Minecraft game.
        Examples include: `int`, `double`, `float`, `entity type`, `item type`, `block pos`...
        """)
    @DocStringProvider(id = 1754726568501L, value = """
        This `argument type adapter` is used to `handle` a `non-vanilla` Minecraft argument type.
        A `non-vanilla` Minecraft argument type is registered by a `module`.
        Examples include: `home name`, `warp name`, `jail name`...
        """)
    private List<Component> toDocumentTexts(@NotNull BaseArgumentTypeAdapter entity) {
        String docString;
        if (entity.isVanillaMinecraftArgumentType()) {
            docString = DocumentUtil.getDocString(getPlayer(), 1754726496693L);
        } else {
            docString = DocumentUtil.getDocString(getPlayer(), 1754726568501L);
        }

        return TextHelper.getDocumentTextList(getPlayer(), docString);
    }

    @Override
    protected @NotNull GuiElementInterface toGuiElement(@NotNull BaseArgumentTypeAdapter entity) {
        List<Component> lore = new ArrayList<>();
        lore.add(TextHelper.getTextByKey(getPlayer(), "from_module", entity.getSourceModule()));
        lore.add(TextHelper.getTextByKey(getPlayer(), "command.argument.type.is_vanilla", entity.isVanillaMinecraftArgumentType()));
        lore.add(TextHelper.getTextByKey(getPlayer(), "command.argument.type.class", entity.getTypeClasses().stream().map(Class::getSimpleName).toList()));
        lore.add(TextHelper.getTextByKey(getPlayer(), "command.argument.type.name", entity.getTypeNames()));
        lore.add(TextHelper.TEXT_EMPTY);
        lore.addAll(toDocumentTexts(entity));


        GuiElementBuilder guiElementBuilder = new GuiElementBuilder()
            .setName(Component.literal(entity.getClass().getSimpleName()))
            .setItem(toItem(entity))
            .setLore(lore);

        if (!entity.isVanillaMinecraftArgumentType()) {
            guiElementBuilder.glow();
        }

        return guiElementBuilder
            .build();
    }

}
