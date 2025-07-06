package io.github.sakurawald.fuji.core.gui.impl.gui;

import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import net.minecraft.server.network.ServerPlayerEntity;

public abstract class ConfirmSignGui extends InputSignGui {

    public ConfirmSignGui(ServerPlayerEntity player) {
        super(player, TextHelper.getTextByKeyWithKeyword(player, "prompt.input.confirm", "confirm"));
    }

    @Override
    public void onClose() {
        if (!this.isConfirmed()) {
            TextHelper.sendActionBarByKey(player, "operation.cancelled");
            onCancelled();
            return;
        }
        onConfirm();
    }

    protected void onCancelled() {}

    private boolean isConfirmed() {
        String confirmationString = TextHelper.Mapper.getValueByKeyword(getPlayer(), "confirm");
        return this.getLine(0).getString().equals(confirmationString);
    }

    public abstract void onConfirm();
}
