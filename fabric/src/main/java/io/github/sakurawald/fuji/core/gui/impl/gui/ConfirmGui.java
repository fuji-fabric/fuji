package io.github.sakurawald.fuji.core.gui.impl.gui;

import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import net.minecraft.server.network.ServerPlayerEntity;

public abstract class ConfirmGui extends InputSignGui {

    public ConfirmGui(ServerPlayerEntity player) {
        super(player, TextHelper.getTextByKeyWithKeyword(player, "prompt.input.confirm", "confirm"));
    }

    @Override
    public void onClose() {
        if (!this.isConfirmed()) {
            TextHelper.sendActionBarByKey(player, "operation.cancelled");
            return;
        }
        onConfirm();
    }

    private boolean isConfirmed() {
        String confirmationString = TextHelper.getValueByKeyword(getPlayer(), "confirm");
        return this.getLine(0).getString().equals(confirmationString);
    }

    public abstract void onConfirm();
}
