package io.github.sakurawald.fuji.module.initializer.echo.send_dialog.structure;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.GuiHelper;
import io.github.sakurawald.fuji.core.command.executor.CommandExecutor;
import io.github.sakurawald.fuji.core.command.executor.structure.ExtendedCommandSource;
import net.minecraft.item.Item;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class DialogGui extends SimpleGui {

    private final int rows;
    private final Text title;

    private final int yesButtonSlotIndex;
    private final Item yesButtonItem;
    private final Text yesButtonName;
    private final String yesButtonCommand;

    private final int noButtonSlotIndex;
    private final Item noButtonItem;
    private final Text noButtonName;
    private final String noButtonCommand;

    private boolean reopenThisGUi = true;
    private final boolean canCloseUsingNoButton;

    private DialogGui(ServerPlayerEntity player, Text title, int rows, int yesButtonSlotIndex, Item yesButtonItem, Text yesButtonName, String yesButtonCommand
    , int noButtonSlotIndex, Item noButtonItem, Text noButtonName, String noButtonCommand, boolean canCloseUsingNoButton) {
        super(GuiHelper.Handler.getGenericContainerType(rows), player, false);
        /* Remember the variables. */
        this.title = title;
        this.rows = rows;

        this.yesButtonSlotIndex = yesButtonSlotIndex;
        this.yesButtonItem = yesButtonItem;
        this.yesButtonName = yesButtonName;
        this.yesButtonCommand = yesButtonCommand;

        this.noButtonSlotIndex = noButtonSlotIndex;
        this.noButtonItem = noButtonItem;
        this.noButtonName = noButtonName;
        this.noButtonCommand = noButtonCommand;
        this.canCloseUsingNoButton = canCloseUsingNoButton;

        /* Use the variables. */
        this.setTitle(title);

        if (GuiHelper.Validator.isValidSlotIndex(this, this.noButtonSlotIndex)) {
            setSlot(this.noButtonSlotIndex, new GuiElementBuilder()
                .setItem(noButtonItem)
                .setName(noButtonName)
                .setCallback(() -> onNoButtonClicked(player, noButtonCommand))
            );
        }

        // NOTE: Place the yes button later, to ensure the yes-button will not be overridden by no-button.
        setSlot(this.yesButtonSlotIndex, new GuiElementBuilder()
            .setItem(yesButtonItem)
            .setName(yesButtonName)
            .setCallback(() -> onYesButtonClicked(player, yesButtonCommand))
        );

        GuiHelper.Filler.fillEmptySlots(this, GuiHelper.Button.makeSlotPlaceholderButton());
    }

    private void onNoButtonClicked(ServerPlayerEntity player, String noButtonCommand) {
        if (this.canCloseUsingNoButton) {
            reopenThisGUi = false;
        }
        close();

        if (!noButtonCommand.isBlank()) {
            CommandExecutor.execute(ExtendedCommandSource.asConsole(player.getCommandSource()), noButtonCommand);
        }
    }

    private void onYesButtonClicked(ServerPlayerEntity player, String yesButtonCommand) {
        reopenThisGUi = false;
        close();

        if (!yesButtonCommand.isBlank()) {
            CommandExecutor.execute(ExtendedCommandSource.asConsole(player.getCommandSource()), yesButtonCommand);
        }
    }

    public static DialogGui makeDialogGui(int rows, ServerPlayerEntity player, Text title
        , int yesButtonSlotIndex, Item yesButtonItem, Text yesButtonName, String yesButtonCommand
        , int noButtonSlotIndex, Item noButtonItem, Text noButtonName, String noButtonCommand
        , boolean canCloseUsingNoButton) {
        return new DialogGui(player, title, rows, yesButtonSlotIndex, yesButtonItem, yesButtonName, yesButtonCommand, noButtonSlotIndex, noButtonItem, noButtonName, noButtonCommand, canCloseUsingNoButton);
    }

    @Override
    public void onClose() {
        if (shouldReopenDialogGui()) {
            makeDialogGui(rows, player, title, yesButtonSlotIndex, yesButtonItem, yesButtonName, yesButtonCommand, noButtonSlotIndex, noButtonItem, noButtonName, noButtonCommand, canCloseUsingNoButton)
                .open();
        }
    }

    private boolean shouldReopenDialogGui() {
        // NOTE: Should not re-open the dialog GUI if the player is dead.
        return !this.player.isRemoved()
            && this.player.isAlive()
            && this.reopenThisGUi;
    }
}
