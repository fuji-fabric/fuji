package io.github.sakurawald.fuji.module.initializer.head.gui;

import com.mojang.authlib.GameProfile;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.AnvilInputGui;
import eu.pb4.sgui.api.gui.SimpleGui;
import io.github.sakurawald.fuji.core.auxiliary.AsyncUtil;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.EntityHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.GuiHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.ItemStackHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.fuji.core.service.gameprofile_fetcher.MojangProfileFetcher;
import io.github.sakurawald.fuji.module.initializer.head.HeadInitializer;
import io.github.sakurawald.fuji.module.initializer.head.structure.EconomyType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

public class PlayerHeadGui extends AnvilInputGui {

    private final @NotNull SimpleGui parentGui;
    private long apiDebounceTimeMs = 0;

    public PlayerHeadGui(@NotNull HeadGui parentGui) {
        super(parentGui.getPlayer(), false);
        this.parentGui = parentGui;
        this.setTitle(TextHelper.getTextByKey(player, "head.category.player"));
        this.setSlot(1, GuiHelper.Validator.makeBannedSlotPlaceholder());
        this.resetResultSlot();
    }

    @Override
    public void setDefaultInputValue(String input) {
        this.setSlot(0, GuiHelper.Validator.makeBannedSlotPlaceholder());
        super.setDefaultInputValue("");
    }

    @Override
    public void onInput(String input) {
        super.onInput(input);
        apiDebounceTimeMs = System.currentTimeMillis() + 500;
    }

    @Override
    public void onClose() {
        parentGui.open();
    }

    private void resetResultSlot() {
        this.setSlot(2, GuiHelper.Validator.makeBannedSlotPlaceholder());
    }

    @Override
    public void onTick() {
        /* Control api debounce. */
        if (apiDebounceTimeMs != 0 && apiDebounceTimeMs <= System.currentTimeMillis()) {
            apiDebounceTimeMs = 0;

            AsyncUtil.runAsyncAndSwallowExceptions(() -> {
                /* Make gui element. */
                GameProfile gameProfile = MojangProfileFetcher.makeOnlineGameProfile(this.getInput());
                GuiElementBuilder builder = new GuiElementBuilder()
                    .setItem(Items.PLAYER_HEAD)
                    .setSkullOwner(gameProfile, EntityHelper.getMinecraftServer(player));

                /* Make head stack. */
                if (HeadInitializer.config.model().economy_type != EconomyType.FREE) {
                    builder.addLoreLine(Text.empty());
                    builder.addLoreLine(TextHelper.getTextByKey(player, "head.price").copy().append(EconomyType.getCostText()));
                }

                /* Set click callback to purchase. */
                ItemStack headStack = builder.asStack();
                this.setSlot(2, headStack, (index, type, action, gui) ->
                    EconomyType.tryPurchaseHeads(player, 1, () -> {
                        var cursorStack = getPlayer().currentScreenHandler.getCursorStack();
                        if (player.currentScreenHandler.getCursorStack().isEmpty()) {
                            player.currentScreenHandler.setCursorStack(headStack.copy());
                        } else if (ItemStackHelper.canCombine(headStack, cursorStack) && cursorStack.getCount() < cursorStack.getMaxCount()) {
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
