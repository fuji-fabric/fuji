package io.github.sakurawald.fuji.module.initializer.kit.gui;

import eu.pb4.sgui.api.gui.SimpleGui;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.fuji.module.initializer.kit.structure.Kit;
import java.util.List;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;

public class KitPreviewGui extends SimpleGui {

    private KitPreviewGui(@NotNull ServerPlayerEntity player, @NotNull Kit kit) {
        super(ScreenHandlerType.GENERIC_9X5, player, false);

        this.setTitle(TextHelper.getTextByKey(player, "kit.preview.gui.title", kit.getName()));

        List<ItemStack> stackList = kit.getStackList();
        for (int i = 0; i < stackList.size(); i++) {
            this.setSlot(i, stackList.get(i));
        }
    }

    public static KitPreviewGui make(@NotNull ServerPlayerEntity player, Kit kit) {
        return new KitPreviewGui(player, kit);
    }



}
