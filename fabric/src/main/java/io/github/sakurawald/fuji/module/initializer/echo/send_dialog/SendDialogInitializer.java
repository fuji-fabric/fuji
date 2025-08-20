package io.github.sakurawald.fuji.module.initializer.echo.send_dialog;

import io.github.sakurawald.fuji.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.fuji.core.command.annotation.CommandNode;
import io.github.sakurawald.fuji.core.command.annotation.CommandRequirement;
import io.github.sakurawald.fuji.core.command.annotation.CommandSource;
import io.github.sakurawald.fuji.core.command.argument.wrapper.impl.GreedyString;
import io.github.sakurawald.fuji.core.command.argument.wrapper.impl.ItemStackWrapper;
import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.module.initializer.ModuleInitializer;
import io.github.sakurawald.fuji.module.initializer.echo.send_dialog.structure.DialogGui;
import java.util.Optional;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

@Document(id = 1751826984411L, value = """
    Send text using the `dialog GUI`.

    For example:
    1. `/send-dialog Steve \\<blue\\>Hello`
    2. `/send-dialog Steve --noButtonSlotIndex -1 \\<green\\>Confirm me.`
    3. `/send-dialog Steve --yesButtonCommand "say confirmed" \\<blue\\>Confirm me.`
    """)

public class SendDialogInitializer extends ModuleInitializer {

    @Document(id = 1751826986144L, value = "Send a dialog GUI to a player.")
    @CommandNode("send-dialog")
    @CommandRequirement(level = 4)
    private static int $sendDialog(@CommandSource ServerCommandSource source
        , ServerPlayerEntity player
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
        Text dialogTitleText = TextHelper.getTextByValue(player, $title);

        int $yesButtonSlotIndex = yesButtonSlotIndex.orElse(2);
        ItemStack $yesButtonItem = yesButtonItem
            .map(ItemStackWrapper::getItemStack)
            .orElse(Items.SLIME_BALL.getDefaultStack());
        String $yesButtonName = yesButtonName.orElse("<green><b>YES");
        Text $$yesButtonName = TextHelper.getTextByValue(player, $yesButtonName);
        String $yesButtonCommand = yesButtonCommand.orElse("");

        int $noButtonSlotIndex = noButtonSlotIndex.orElse(6);
        ItemStack $noButtonItem = noButtonItem
            .map(ItemStackWrapper::getItemStack)
            .orElse(Items.MAGMA_CREAM.getDefaultStack());
        String $noButtonName = noButtonName.orElse("<dark_red><b>NO");
        Text $$noButtonName = TextHelper.getTextByValue(player, $noButtonName);
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
