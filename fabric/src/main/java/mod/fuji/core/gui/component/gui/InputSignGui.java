package mod.fuji.core.gui.component.gui;

import eu.pb4.sgui.api.gui.SignGui;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.DyeColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class InputSignGui extends SignGui {

    public InputSignGui(@NotNull ServerPlayer player, @Nullable Component promptText) {
        super(player);
        this.setSignType(Blocks.CHERRY_WALL_SIGN);
        this.setColor(DyeColor.BLACK);
        if (promptText != null) {
            this.setLine(3, promptText);
        }
        this.setAutoUpdate(false);
    }

    @SuppressWarnings("UnnecessaryLocalVariable")
    protected @NotNull String joinStrings() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 4; i++) {
            String line = this.getLine(i).getString().trim();
            sb.append(line);
        }
        String joinedString = sb.toString().trim();
        return joinedString;
    }

    protected boolean isEmptyInput() {
        return this.joinStrings().isBlank();
    }

    public void onVirtualGuiClose() {

    }

    #if MC_VER < MC_26_1
    @Override
    public final void onClose() {
        this.onVirtualGuiClose();
    }
    #elif MC_VER >= MC_26_1
    @Override
    public final void onManualClose() {
        this.onVirtualGuiClose();
    }
    #endif

}
