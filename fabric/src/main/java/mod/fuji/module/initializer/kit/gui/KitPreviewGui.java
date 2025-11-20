package mod.fuji.module.initializer.kit.gui;

import eu.pb4.sgui.api.gui.SimpleGui;
import mod.fuji.core.auxiliary.minecraft.TextHelper;
import mod.fuji.module.initializer.kit.structure.Kit;
import java.util.List;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

public class KitPreviewGui extends SimpleGui {

    private KitPreviewGui(@NotNull ServerPlayer player, @NotNull Kit kit) {
        super(MenuType.GENERIC_9x5, player, false);

        this.setTitle(TextHelper.getTextByKey(player, "kit.preview.gui.title", kit.getName()));

        List<ItemStack> stackList = kit.getStackList();
        for (int i = 0; i < stackList.size(); i++) {
            this.setSlot(i, stackList.get(i));
        }
    }

    public static KitPreviewGui make(@NotNull ServerPlayer player, Kit kit) {
        return new KitPreviewGui(player, kit);
    }



}
