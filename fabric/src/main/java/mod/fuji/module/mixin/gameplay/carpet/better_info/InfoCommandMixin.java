package mod.fuji.module.mixin.gameplay.carpet.better_info;

import carpet.commands.InfoCommand;
import mod.fuji.core.auxiliary.minecraft.TextHelper;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(InfoCommand.class)
public class InfoCommandMixin {

    @Inject(method = "infoBlock", at = @At(value = "INVOKE", target = "Lcarpet/commands/InfoCommand;printBlock(Ljava/util/List;Lnet/minecraft/commands/CommandSourceStack;Ljava/lang/String;)V", shift = At.Shift.AFTER))
    private static void addNbtQueryForInfoBlockCommand(@NotNull CommandSourceStack source, @NotNull BlockPos pos, String grep, CallbackInfoReturnable<Integer> cir) {
        MutableComponent additionalText = Component.empty().append("\n");

        BlockEntity blockEntity = source.getLevel().getBlockEntity(pos);
        if (blockEntity == null) {
            TextHelper.sendMessageByText(source, additionalText.append(Component.literal("No block entity found at " + pos.getX() + ", " + pos.getY() + ", " + pos.getZ())));
            return;
        }

        #if MC_VER <= MC_1_20_4
        NbtCompound compoundTag = blockEntity.createNbtWithIdentifyingData();
        #elif MC_VER > MC_1_20_4
        CompoundTag compoundTag = blockEntity.saveWithFullMetadata(blockEntity.getLevel().registryAccess());
        #endif
        MutableComponent nbtDataText = Component.translatable("commands.data.block.query", pos.getX(), pos.getY(), pos.getZ(), NbtUtils.toPrettyComponent(compoundTag));
        additionalText = additionalText.append(nbtDataText);

        TextHelper.sendMessageByText(source, additionalText);
    }
}
