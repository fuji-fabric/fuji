package io.github.sakurawald.fuji.core.gui.component.gui;

import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;

public abstract class ConfirmSignGui extends InputSignGui {

    public ConfirmSignGui(@NotNull ServerPlayerEntity player) {
        super(player, TextHelper.getTextByKeyAndReplaceTheKeyword(player, "prompt.input.confirm", "confirm"));
    }

    @Override
    public final void onClose() {
        this.onConfirmedOrCancelled();

        if (!this.isConfirmed()) {
            TextHelper.sendTextByKey(player, "operation.cancelled");
            this.onCancelled();
            return;
        }
        this.onConfirm();
    }

    protected void onCancelled() {}

    protected void onConfirmedOrCancelled() {}

    private boolean isConfirmed() {
        String confirmationString = TextHelper.Translator.getLanguageValueByKey(getPlayer(), "keyword.confirm");
        return this.getLine(0).getString().equalsIgnoreCase(confirmationString);
    }

    public abstract void onConfirm();
}
