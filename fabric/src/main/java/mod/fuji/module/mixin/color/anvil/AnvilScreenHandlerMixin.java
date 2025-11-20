package mod.fuji.module.mixin.color.anvil;

import mod.fuji.core.auxiliary.minecraft.PlayerHelper;
import mod.fuji.core.auxiliary.minecraft.TextHelper;
import mod.fuji.module.initializer.color.anvil.ColorAnvilInitializer;
import java.util.concurrent.atomic.AtomicReference;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.ItemCombinerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.MenuType;
#if MC_VER > MC_1_21
import net.minecraft.world.inventory.ItemCombinerMenuSlotDefinition;
#endif

import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(AnvilMenu.class)
public abstract class AnvilScreenHandlerMixin extends ItemCombinerMenu {

    @Shadow
    private String itemName;

    #if MC_VER <= MC_1_21
    public AnvilScreenHandlerMixin(@Nullable MenuType<?> screenHandlerType, int i, Inventory playerInventory, ContainerLevelAccess screenHandlerContext) {
        super(screenHandlerType, i, playerInventory, screenHandlerContext);
    }
    #elif MC_VER > MC_1_21
    public AnvilScreenHandlerMixin(@Nullable MenuType<?> screenHandlerType, int i, Inventory playerInventory, ContainerLevelAccess screenHandlerContext, ItemCombinerMenuSlotDefinition forgingSlotsManager) {
        super(screenHandlerType, i, playerInventory, screenHandlerContext, forgingSlotsManager);
    }
    #endif

    @Unique
    private @NotNull Component parseInputNewItemName() {
        AtomicReference<Component> modifiedText = new AtomicReference<>();
        PlayerHelper.Kind.withServerPlayerEntity(player, () -> {
            /* Stripe style tags. */
            if (ColorAnvilInitializer.config.model().requires_corresponding_permission_to_use_style_tag) {
                Player player = super.player;
                itemName = ColorAnvilInitializer.stripeStyleTags(player, itemName);
            }
            modifiedText.set(TextHelper.getTextByValue(null, itemName));
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
    @ModifyArg(method = "createResult", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;set(Lnet/minecraft/core/component/DataComponentType;Ljava/lang/Object;)Ljava/lang/Object;", ordinal = 0))
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
    @ModifyArg(method = "setItemName", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;set(Lnet/minecraft/core/component/DataComponentType;Ljava/lang/Object;)Ljava/lang/Object;", ordinal = 0))
    public @NotNull Object newItemName(Object text)
    #endif
    {
         return parseInputNewItemName();
    }
}
