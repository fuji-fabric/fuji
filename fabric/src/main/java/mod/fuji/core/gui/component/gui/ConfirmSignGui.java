package mod.fuji.core.gui.component.gui;

import mod.fuji.core.auxiliary.minecraft.TextHelper;
import mod.fuji.core.gui.structure.SimpleGuiDuck;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class ConfirmSignGui extends InputSignGui {

    final SimpleGuiDuck parent;

    public ConfirmSignGui(@Nullable SimpleGuiDuck parent, @NotNull ServerPlayer player) {
        super(player, TextHelper.getTextByKeyAndReplaceTheKeyword(player, "prompt.input.confirm", "confirm"));
        this.parent = parent;
    }

    public ConfirmSignGui(@NotNull ServerPlayer player) {
        this(null, player);
    }

    @Override
    public final void onVirtualGuiClose() {
        this.onConfirmedOrCancelled();

        if (!this.isConfirmed()) {
            TextHelper.sendTextByKey(player, "operation.cancelled");
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
