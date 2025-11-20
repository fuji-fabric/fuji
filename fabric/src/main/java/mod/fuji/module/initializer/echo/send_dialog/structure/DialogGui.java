package mod.fuji.module.initializer.echo.send_dialog.structure;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import mod.fuji.core.auxiliary.minecraft.CommandHelper;
import mod.fuji.core.auxiliary.minecraft.GuiHelper;
import mod.fuji.core.command.executor.CommandExecutor;
import mod.fuji.core.command.executor.structure.ExtendedCommandSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;

public class DialogGui extends SimpleGui {

    private final int rows;
    private final Component title;

    private final int yesButtonSlotIndex;
    private final ItemStack yesButtonItem;
    private final Component yesButtonName;
    private final String yesButtonCommand;

    private final int noButtonSlotIndex;
    private final ItemStack noButtonItem;
    private final Component noButtonName;
    private final String noButtonCommand;

    private boolean reopenThisGUi = true;
    private final boolean canCloseUsingNoButton;

    private DialogGui(ServerPlayer player, Component title, int rows, int yesButtonSlotIndex, ItemStack yesButtonItem, Component yesButtonName, String yesButtonCommand
    , int noButtonSlotIndex, ItemStack noButtonItem, Component noButtonName, String noButtonCommand, boolean canCloseUsingNoButton) {
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
            setSlot(this.noButtonSlotIndex, GuiElementBuilder
                .from(noButtonItem)
                .setName(noButtonName)
                .setCallback(() -> onNoButtonClicked(player, noButtonCommand))
            );
        }

        // NOTE: Place the yes button later, to ensure the yes-button will not be overridden by no-button.
        setSlot(this.yesButtonSlotIndex, GuiElementBuilder
            .from(yesButtonItem)
            .setName(yesButtonName)
            .setCallback(() -> onYesButtonClicked(player, yesButtonCommand))
        );

        GuiHelper.Placer.fillEmptySlots(this, GuiHelper.Button.makeSlotPlaceholderButton());
    }

    private void onNoButtonClicked(ServerPlayer player, String noButtonCommand) {
        if (this.canCloseUsingNoButton) {
            reopenThisGUi = false;
        }
        close();

        if (!noButtonCommand.isBlank()) {
            CommandExecutor.executeSingle(ExtendedCommandSource.asConsole(CommandHelper.Source.getCommandSource(player)), noButtonCommand);
        }
    }

    private void onYesButtonClicked(ServerPlayer player, String yesButtonCommand) {
        reopenThisGUi = false;
        close();

        if (!yesButtonCommand.isBlank()) {
            CommandExecutor.executeSingle(ExtendedCommandSource.asConsole(CommandHelper.Source.getCommandSource(player)), yesButtonCommand);
        }
    }

    public static DialogGui makeDialogGui(int rows, ServerPlayer player, Component title
        , int yesButtonSlotIndex, ItemStack yesButtonItem, Component yesButtonName, String yesButtonCommand
        , int noButtonSlotIndex, ItemStack noButtonItem, Component noButtonName, String noButtonCommand
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
