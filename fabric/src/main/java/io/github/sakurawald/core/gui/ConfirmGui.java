package io.github.sakurawald.core.gui;

import io.github.sakurawald.core.auxiliary.minecraft.TextHelper;
import net.minecraft.server.network.ServerPlayerEntity;

public abstract class ConfirmGui extends InputSignGui {

    public ConfirmGui(ServerPlayerEntity player) {
        super(player, TextHelper.getTextByKeyWithKeyword(player, "prompt.input.confirm", "confirm"));
    }

    @Override
    public void onClose() {
        String string = TextHelper.getKeywordValue(getPlayer(), "confirm");
        if (!this.getLine(0).getString().equals(string)) {
            TextHelper.sendActionBarByKey(player, "operation.cancelled");
            return;
        }
        onConfirm();
    }

    public abstract void onConfirm();
}
