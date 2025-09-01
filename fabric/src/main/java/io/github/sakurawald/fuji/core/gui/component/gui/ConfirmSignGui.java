package io.github.sakurawald.fuji.core.gui.component.gui;

import eu.pb4.sgui.api.gui.SlotGuiInterface;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class ConfirmSignGui extends InputSignGui {

    final SlotGuiInterface parent;

    public ConfirmSignGui(@Nullable SlotGuiInterface parent, @NotNull ServerPlayerEntity player) {
        super(player, TextHelper.getTextByKeyAndReplaceTheKeyword(player, "prompt.input.confirm", "confirm"));
        this.parent = parent;
    }

    public ConfirmSignGui(@NotNull ServerPlayerEntity player) {
        this(null, player);
    }

    @Override
    public final void onClose() {
        this.onConfirmedOrCancelled();

        if (!this.isConfirmed()) {
            Text cancelledText = TextHelper.getTextByKey(player, "operation.cancelled");
            TextHelper.sendToastByText(player, Items.BARRIER.getDefaultStack(), cancelledText);

            this.onCancelled();
            return;
        }
        this.onConfirm();
    }

    protected void onCancelled() {}

    protected void onConfirmedOrCancelled() {
        if (this.parent != null) {
            this.parent.open();
        }
    }

    private boolean isConfirmed() {
        String confirmationString = TextHelper.Translator.getLanguageValueByKey(getPlayer(), "keyword.confirm");
        return this.getLine(0).getString().equalsIgnoreCase(confirmationString);
    }

    public abstract void onConfirm();
}
