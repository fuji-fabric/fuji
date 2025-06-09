package io.github.sakurawald.module.mixin.color.anvil;

import io.github.sakurawald.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.core.service.style_striper.StyleStriper;
import io.github.sakurawald.module.initializer.color.anvil.ColorAnvilInitializer;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.AnvilScreenHandler;
import net.minecraft.screen.ForgingScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.ForgingSlotsManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(AnvilScreenHandler.class)
public abstract class AnvilScreenHandlerMixin extends ForgingScreenHandler {

    @Unique
    private static final String STYLE_TYPE_ANVIL = "anvil";

    @Shadow
    private String newItemName;

    public AnvilScreenHandlerMixin(@Nullable ScreenHandlerType<?> screenHandlerType, int i, PlayerInventory playerInventory, ScreenHandlerContext screenHandlerContext, ForgingSlotsManager forgingSlotsManager) {
        super(screenHandlerType, i, playerInventory, screenHandlerContext, forgingSlotsManager);
    }

    @ModifyArg(method = "updateResult", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;set(Lnet/minecraft/component/ComponentType;Ljava/lang/Object;)Ljava/lang/Object;", ordinal = 0))
    public @NotNull Object updateResult(Object text) {
        if (ColorAnvilInitializer.config.model().requires_corresponding_permission_to_use_style_tag) {
            newItemName = StyleStriper.stripe(super.player, STYLE_TYPE_ANVIL, newItemName);
        }
        return TextHelper.getTextByValue(null, newItemName);
    }

    @ModifyArg(method = "setNewItemName", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;set(Lnet/minecraft/component/ComponentType;Ljava/lang/Object;)Ljava/lang/Object;", ordinal = 0))
    public @NotNull Object newItemName(Object text) {
        if (ColorAnvilInitializer.config.model().requires_corresponding_permission_to_use_style_tag) {
            newItemName = StyleStriper.stripe(super.player, STYLE_TYPE_ANVIL, newItemName);
        }
        return TextHelper.getTextByValue(null, newItemName);
    }
}
