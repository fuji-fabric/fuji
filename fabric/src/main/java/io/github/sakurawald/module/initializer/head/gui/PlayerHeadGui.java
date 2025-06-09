package io.github.sakurawald.module.initializer.head.gui;

import com.mojang.authlib.GameProfile;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.AnvilInputGui;
import eu.pb4.sgui.api.gui.SimpleGui;
import io.github.sakurawald.core.auxiliary.minecraft.GuiHelper;
import io.github.sakurawald.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.core.service.gameprofile_fetcher.MojangProfileFetcher;
import io.github.sakurawald.module.initializer.head.HeadInitializer;
import io.github.sakurawald.module.initializer.head.structure.EconomyType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class PlayerHeadGui extends AnvilInputGui {

    private final @NotNull SimpleGui parentGui;
    private long apiDebounce = 0;

    public PlayerHeadGui(@NotNull HeadGui parentGui) {
        super(parentGui.player, false);
        this.parentGui = parentGui;
        this.setTitle(TextHelper.getTextByKey(player, "head.category.player"));
        this.setSlot(1, GuiHelper.makeBarrier());
        this.resetResultSlot();
    }

    @Override
    public void setDefaultInputValue(String input) {
        this.setSlot(0, GuiHelper.makeBarrier());
        super.setDefaultInputValue("");
    }

    @Override
    public void onInput(String input) {
        super.onInput(input);
        apiDebounce = System.currentTimeMillis() + 500;
    }

    @Override
    public void onClose() {
        parentGui.open();
    }

    private void resetResultSlot() {
        this.setSlot(2, GuiHelper.makeBarrier());
    }

    @Override
    public void onTick() {
        if (apiDebounce != 0 && apiDebounce <= System.currentTimeMillis()) {
            apiDebounce = 0;

            CompletableFuture.runAsync(() -> {
                /* make gui element */
                GameProfile gameProfile = MojangProfileFetcher.makeOnlineGameProfile(this.getInput());
                GuiElementBuilder builder = new GuiElementBuilder()
                    .setItem(Items.PLAYER_HEAD)
                    .setSkullOwner(gameProfile, player.server);

                /* make head item */
                if (HeadInitializer.head.model().economy_type != EconomyType.FREE) {
                    builder.addLoreLine(Text.empty());
                    builder.addLoreLine(TextHelper.getTextByKey(player, "head.price").copy().append(EconomyType.getCostText()));
                }

                ItemStack headStack = builder.asStack();

                /* purchase */
                this.setSlot(2, headStack, (index, type, action, gui) ->
                    EconomyType.tryPurchase(player, 1, () -> {
                        var cursorStack = getPlayer().currentScreenHandler.getCursorStack();
                        if (player.currentScreenHandler.getCursorStack().isEmpty()) {
                            player.currentScreenHandler.setCursorStack(headStack.copy());
                        } else if (ItemStack.areItemsAndComponentsEqual(headStack, cursorStack) && cursorStack.getCount() < cursorStack.getMaxCount()) {
                            cursorStack.increment(1);
                        } else {
                            player.dropItem(headStack.copy(), false);
                        }
                    })
                );

            });
        }
    }

}
