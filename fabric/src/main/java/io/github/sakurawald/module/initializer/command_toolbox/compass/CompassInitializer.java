package io.github.sakurawald.module.initializer.command_toolbox.compass;

import io.github.sakurawald.core.annotation.Document;
import io.github.sakurawald.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.core.auxiliary.minecraft.EntityHelper;
import io.github.sakurawald.core.auxiliary.minecraft.NbtHelper;
import io.github.sakurawald.core.auxiliary.minecraft.RegistryHelper;
import io.github.sakurawald.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.core.command.annotation.CommandNode;
import io.github.sakurawald.core.command.annotation.CommandSource;
import io.github.sakurawald.core.command.annotation.CommandTarget;
import io.github.sakurawald.core.command.argument.wrapper.impl.Dimension;
import io.github.sakurawald.module.initializer.ModuleInitializer;
#if MC_VER <= MC_1_20_4
#elif MC_VER > MC_1_20_4
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LodestoneTrackerComponent;
import net.minecraft.util.math.GlobalPos;
import java.util.Optional;
#endif

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

@CommandNode("compass")
public class CompassInitializer extends ModuleInitializer {

    private static int withCompassInHand(ServerPlayerEntity source, Function<ItemStack, Integer> function) {
        ItemStack itemStack = source.getMainHandStack();
        if (!itemStack.getItem().equals(Items.COMPASS)) {
            TextHelper.sendMessageByKey(source, "compass.no_compass");
            return CommandHelper.Return.FAIL;
        }

        return function.apply(itemStack);
    }

    private static void setTrackedTarget(ItemStack itemStack, @Nullable ServerWorld world, @Nullable BlockPos blockPos) {

        #if MC_VER <= MC_1_20_4
        NbtHelper.withNbt(itemStack, tag -> {
            if (world == null) {
                tag.remove("LodestoneDimension");
            } else {
                tag.putString("LodestoneDimension", RegistryHelper.ofString(world));
            }

            if (blockPos == null) {
                tag.remove("LodestonePos");
            } else {
                NbtCompound posTag = new NbtCompound();
                posTag.putInt("X", blockPos.getX());
                posTag.putInt("Y", blockPos.getY());
                posTag.putInt("Z", blockPos.getZ());
                tag.put("LodestonePos", posTag);
            }

        });

        #elif MC_VER > MC_1_20_4
        LodestoneTrackerComponent component = new LodestoneTrackerComponent(Optional.of(GlobalPos.create(world.getRegistryKey(), blockPos)), false);
        itemStack.set(DataComponentTypes.LODESTONE_TRACKER, component);
        #endif

    }

    @CommandNode("track pos")
    @Document("Let the compass in hand track a specified position.")
    private static int track(@CommandSource @CommandTarget ServerPlayerEntity player, Dimension dimension, BlockPos blockPos) {
        return withCompassInHand(player, (itemStack) -> {
            setTrackedTarget(itemStack, dimension.getValue(), blockPos);
            return CommandHelper.Return.SUCCESS;
        });
    }

    @CommandNode("track player")
    @Document("Let the compass in hand track a specified player.")
    private static int track(@CommandSource @CommandTarget ServerPlayerEntity player, ServerPlayerEntity target) {
        return withCompassInHand(player, (itemStack) -> {
            setTrackedTarget(itemStack, EntityHelper.getServerWorld(target), target.getBlockPos());
            return CommandHelper.Return.SUCCESS;
        });
    }

    @CommandNode("reset")
    @Document("Let the compass in hand track nothing.")
    private static int reset(@CommandSource @CommandTarget ServerPlayerEntity player) {
        return withCompassInHand(player, (itemStack) -> {
            setTrackedTarget(itemStack,null,null);
            return CommandHelper.Return.SUCCESS;
        });
    }
}
