package mod.fuji.module.initializer.head.gui;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.AnvilInputGui;
import eu.pb4.sgui.api.gui.SimpleGui;
import mod.fuji.core.auxiliary.AsyncUtil;
import mod.fuji.core.auxiliary.minecraft.GuiHelper;
import mod.fuji.core.auxiliary.minecraft.ItemStackHelper;
import mod.fuji.core.auxiliary.minecraft.ServerHelper;
import mod.fuji.core.auxiliary.minecraft.TextHelper;
import mod.fuji.core.service.gameprofile_fetcher.MojangProfileFetcher;
import mod.fuji.module.initializer.head.HeadInitializer;
import mod.fuji.module.initializer.head.structure.EconomyType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public class PlayerHeadGui extends AnvilInputGui {

    private final @NotNull SimpleGui parentGui;
    private long apiDebounceTimeMs = 0;

    public PlayerHeadGui(@NotNull HeadGui parentGui) {
        super(parentGui.getPlayer(), false);
        this.parentGui = parentGui;
        this.setTitle(TextHelper.getTextByKey(player, "head.category.player"));
        GuiHelper.setSlot(this, 1, GuiHelper.Validator.makeBannedSlotPlaceholder());
        this.resetResultSlot();
    }

    @Override
    public void setDefaultInputValue(String input) {
        GuiHelper.setSlot(this, 0, GuiHelper.Validator.makeBannedSlotPlaceholder());
        super.setDefaultInputValue("");
    }

    @Override
    public void onInput(String input) {
        super.onInput(input);
        apiDebounceTimeMs = System.currentTimeMillis() + 500;
    }

    @Override
    public void onManualClose() {
        parentGui.open();
    }

    private void resetResultSlot() {
        GuiHelper.setSlot(this, 2, GuiHelper.Validator.makeBannedSlotPlaceholder());
    }

    @Override
    public void onTick() {
        /* Control api debounce. */
        if (apiDebounceTimeMs != 0 && apiDebounceTimeMs <= System.currentTimeMillis()) {
            apiDebounceTimeMs = 0;

            AsyncUtil.runAsyncAndSwallowExceptions(() -> {
                /* Make gui element. */
                GuiElementBuilder builder = new GuiElementBuilder()
                    .setItem(Items.PLAYER_HEAD);

                /* Set skull textures if online game profile is valid. */
                MojangProfileFetcher
                    .fetchOnlineGameProfile(this.getInput())
                    .ifPresent(gameProfile -> GuiHelper.PlayerSkull.setSkullOwner(builder, gameProfile));

                /* Make head stack. */
                if (HeadInitializer.config.model().economy_type != EconomyType.FREE) {
                    builder.addLoreLine(Component.empty());
                    builder.addLoreLine(TextHelper.getTextByKey(player, "head.price").copy().append(EconomyType.getCostText()));
                }

                /* Set click callback to purchase. */
                ItemStack headStack = builder.asStack();
                this.setSlot(2, headStack, (index, type, action, gui) ->
                    EconomyType.tryPurchaseHeads(player, 1, () -> {
                        var cursorStack = getPlayer().containerMenu.getCarried();
                        if (player.containerMenu.getCarried().isEmpty()) {
                            player.containerMenu.setCarried(headStack.copy());
                        } else if (ItemStackHelper.canCombine(headStack, cursorStack) && cursorStack.getCount() < cursorStack.getMaxStackSize()) {
                            cursorStack.grow(1);
                        } else {
                            player.drop(headStack.copy(), false);
                        }
                    })
                );

            });
        }
    }

}
