package mod.fuji.module.initializer.echo.send_dialog;

import mod.fuji.core.auxiliary.minecraft.CommandHelper;
import mod.fuji.core.auxiliary.minecraft.TextHelper;
import mod.fuji.core.command.annotation.CommandNode;
import mod.fuji.core.command.annotation.CommandRequirement;
import mod.fuji.core.command.annotation.CommandSource;
import mod.fuji.core.command.argument.wrapper.impl.GreedyString;
import mod.fuji.core.command.argument.wrapper.impl.ItemStackWrapper;
import mod.fuji.core.document.annotation.Document;
import mod.fuji.module.initializer.ModuleInitializer;
import mod.fuji.module.initializer.echo.send_dialog.structure.DialogGui;
import java.util.Optional;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;

@Document(id = 1751826984411L, value = """
    Send the `text` using the `dialog GUI`.

    For example:
    1. `/send-dialog Steve \\<blue\\>Hello`
    2. `/send-dialog Steve --noButtonSlotIndex -1 \\<green\\>Confirm me.`
    3. `/send-dialog Steve --yesButtonCommand "say confirmed" \\<blue\\>Confirm me.`
    """)

public class SendDialogInitializer extends ModuleInitializer {

    @Document(id = 1751826986144L, value = "Send a dialog GUI to a player.")
    @CommandNode("send-dialog")
    @CommandRequirement(level = 4)
    private static int $sendDialog(@CommandSource CommandSourceStack source
        , ServerPlayer player
        , Optional<Integer> rows
        , Optional<Integer> yesButtonSlotIndex
        , Optional<ItemStackWrapper> yesButtonItem
        , Optional<String> yesButtonName
        , Optional<String> yesButtonCommand
        , Optional<Integer> noButtonSlotIndex
        , Optional<ItemStackWrapper> noButtonItem
        , Optional<String> noButtonName
        , Optional<String> noButtonCommand
        , Optional<Boolean> canCloseUsingNoButton
        , GreedyString title) {

        /* Extract the arguments. */
        Integer $rows = rows.orElse(1);
        String $title = title.getValue();
        Component dialogTitleText = TextHelper.getTextByValue(player, $title);

        int $yesButtonSlotIndex = yesButtonSlotIndex.orElse(2);
        ItemStack $yesButtonItem = yesButtonItem
            .map(ItemStackWrapper::getItemStack)
            .orElse(Items.SLIME_BALL.getDefaultInstance());
        String $yesButtonName = yesButtonName.orElse("<green><b>YES");
        Component $$yesButtonName = TextHelper.getTextByValue(player, $yesButtonName);
        String $yesButtonCommand = yesButtonCommand.orElse("");

        int $noButtonSlotIndex = noButtonSlotIndex.orElse(6);
        ItemStack $noButtonItem = noButtonItem
            .map(ItemStackWrapper::getItemStack)
            .orElse(Items.MAGMA_CREAM.getDefaultInstance());
        String $noButtonName = noButtonName.orElse("<dark_red><b>NO");
        Component $$noButtonName = TextHelper.getTextByValue(player, $noButtonName);
        String $noButtonCommand = noButtonCommand.orElse("");
        boolean $canCloseUsingNoButton = canCloseUsingNoButton.orElse(false);

        /* Make the dialog GUI. */
        DialogGui.makeDialogGui($rows, player, dialogTitleText
                , $yesButtonSlotIndex, $yesButtonItem, $$yesButtonName, $yesButtonCommand
                , $noButtonSlotIndex, $noButtonItem, $$noButtonName, $noButtonCommand, $canCloseUsingNoButton
            )
            .open();

        return CommandHelper.Return.SUCCESS;
    }

}
