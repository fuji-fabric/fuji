package mod.fuji.module.initializer.jail.gui;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import eu.pb4.sgui.api.gui.SimpleGui;
import mod.fuji.core.auxiliary.minecraft.TextHelper;
import mod.fuji.core.gui.component.gui.PagedGui;
import mod.fuji.module.initializer.jail.service.JailService;
import mod.fuji.module.initializer.jail.structure.JailDescriptor;
import java.util.List;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class JailListGui extends PagedGui<JailDescriptor> {

    public JailListGui(@Nullable SimpleGui parent, @NotNull ServerPlayerEntity player, @NotNull List<JailDescriptor> entities, int pageIndex) {
        super(parent, player, TextHelper.getTextByKey(player, "jail.list.gui.title"), entities, pageIndex);
    }

    @Override
    protected PagedGui<JailDescriptor> makePage(@Nullable SimpleGui parent, @NotNull ServerPlayerEntity player, Text title, @NotNull List<JailDescriptor> entities, int pageIndex) {
        return new JailListGui(parent, player, entities, pageIndex);
    }

    public static JailListGui make(@NotNull ServerPlayerEntity player) {
        List<JailDescriptor> entities = JailService.getJailDescriptors();
        return new JailListGui(null, player, entities, 0);
    }

    @SuppressWarnings("CodeBlock2Expr")
    @Override
    protected @NotNull GuiElementInterface toGuiElement(@NotNull JailDescriptor entity) {
        GuiElementBuilder builder = new GuiElementBuilder();

        builder
            .setItem(Items.IRON_BARS)
            .setName(Text.literal(entity.getId()))
            .setLore(List.of(TextHelper.getTextByKey(this.getPlayer(), "prompt.click.see_inside")))
            .setCallback(() -> {
                JailInfoGui
                    .make(this.getBackendGui(), this.getPlayer(), entity)
                    .open();
            });

        return builder.build();
    }
}
