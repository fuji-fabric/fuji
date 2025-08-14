package io.github.sakurawald.fuji.module.mixin.gameplay.carpet.better_info;

import carpet.commands.InfoCommand;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(InfoCommand.class)
public class InfoCommandMixin {

    @Inject(method = "infoBlock", at = @At(value = "INVOKE", target = "Lcarpet/commands/InfoCommand;printBlock(Ljava/util/List;Lnet/minecraft/server/command/ServerCommandSource;Ljava/lang/String;)V", shift = At.Shift.AFTER))
    private static void addNbtQueryForInfoBlockCommand(@NotNull ServerCommandSource source, @NotNull BlockPos pos, String grep, CallbackInfoReturnable<Integer> cir) {
        MutableText additionalText = Text.empty().append("\n");

        BlockEntity blockEntity = source.getWorld().getBlockEntity(pos);
        if (blockEntity == null) {
            TextHelper.sendMessageByText(source, additionalText.append(Text.literal("No block entity found at " + pos.getX() + ", " + pos.getY() + ", " + pos.getZ())));
            return;
        }

        #if MC_VER <= MC_1_20_4
        NbtCompound compoundTag = blockEntity.createNbtWithIdentifyingData();
        #elif MC_VER > MC_1_20_4
        NbtCompound compoundTag = blockEntity.createNbtWithIdentifyingData(blockEntity.getWorld().getRegistryManager());
        #endif
        MutableText nbtDataText = Text.translatable("commands.data.block.query", pos.getX(), pos.getY(), pos.getZ(), NbtHelper.toPrettyPrintedText(compoundTag));
        additionalText = additionalText.append(nbtDataText);

        TextHelper.sendMessageByText(source, additionalText);
    }
}
