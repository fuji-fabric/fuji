package io.github.sakurawald.fuji.module.mixin.color.anvil;

import io.github.sakurawald.fuji.core.auxiliary.minecraft.ServerHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.fuji.module.initializer.color.anvil.ColorAnvilInitializer;
import java.util.concurrent.atomic.AtomicReference;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.AnvilScreenHandler;
import net.minecraft.screen.ForgingScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.ScreenHandlerType;
#if MC_VER > MC_1_21
import net.minecraft.screen.slot.ForgingSlotsManager;
#endif

import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(AnvilScreenHandler.class)
public abstract class AnvilScreenHandlerMixin extends ForgingScreenHandler {

    @Shadow
    private String newItemName;

    #if MC_VER <= MC_1_21
    public AnvilScreenHandlerMixin(@Nullable ScreenHandlerType<?> screenHandlerType, int i, PlayerInventory playerInventory, ScreenHandlerContext screenHandlerContext) {
        super(screenHandlerType, i, playerInventory, screenHandlerContext);
    }
    #elif MC_VER > MC_1_21
    public AnvilScreenHandlerMixin(@Nullable ScreenHandlerType<?> screenHandlerType, int i, PlayerInventory playerInventory, ScreenHandlerContext screenHandlerContext, ForgingSlotsManager forgingSlotsManager) {
        super(screenHandlerType, i, playerInventory, screenHandlerContext, forgingSlotsManager);
    }
    #endif

    @Unique
    private @NotNull Text parseInputNewItemName() {
        AtomicReference<Text> modifiedText = new AtomicReference<>();
        ServerHelper.withServerPlayerEntity(player, () -> {
            /* Stripe style tags. */
            if (ColorAnvilInitializer.config.model().requires_corresponding_permission_to_use_style_tag) {
                PlayerEntity player = super.player;
                newItemName = ColorAnvilInitializer.stripeStyleTags(player, newItemName);
            }
            modifiedText.set(TextHelper.getTextByValue(null, newItemName));
        });

        return modifiedText.get();
    }

    #if MC_VER <= MC_1_20_4
    @ModifyArg(method = "updateResult", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;setCustomName(Lnet/minecraft/text/Text;)Lnet/minecraft/item/ItemStack;", ordinal = 0))
    public Text updateResult(Text text)
    #elif MC_VER > MC_1_20_4 && MC_VER <= MC_1_20_6
    @ModifyArg(method = "updateResult", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;set(Lnet/minecraft/component/DataComponentType;Ljava/lang/Object;)Ljava/lang/Object;", ordinal = 0))
    public @NotNull Object updateResult(Object text)
    #elif MC_VER > MC_1_20_6
    @ModifyArg(method = "updateResult", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;set(Lnet/minecraft/component/ComponentType;Ljava/lang/Object;)Ljava/lang/Object;", ordinal = 0))
    public @NotNull Object updateResult(Object text)
    #endif
    {
         return parseInputNewItemName();
    }

    #if MC_VER <= MC_1_20_4
    @ModifyArg(method = "setNewItemName", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;setCustomName(Lnet/minecraft/text/Text;)Lnet/minecraft/item/ItemStack;", ordinal = 0))
    public Text newItemName(Text text)
    #elif MC_VER > MC_1_20_4 && MC_VER <= MC_1_20_6
    @ModifyArg(method = "setNewItemName", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;set(Lnet/minecraft/component/DataComponentType;Ljava/lang/Object;)Ljava/lang/Object;", ordinal = 0))
    public @NotNull Object newItemName(Object text)
    #elif MC_VER > MC_1_20_6
    @ModifyArg(method = "setNewItemName", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;set(Lnet/minecraft/component/ComponentType;Ljava/lang/Object;)Ljava/lang/Object;", ordinal = 0))
    public @NotNull Object newItemName(Object text)
    #endif
    {
         return parseInputNewItemName();
    }
}
