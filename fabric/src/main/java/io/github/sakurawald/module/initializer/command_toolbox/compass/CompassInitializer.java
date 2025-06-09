package io.github.sakurawald.module.initializer.command_toolbox.compass;

import io.github.sakurawald.core.annotation.Document;
import io.github.sakurawald.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.core.command.annotation.CommandNode;
import io.github.sakurawald.core.command.annotation.CommandSource;
import io.github.sakurawald.core.command.annotation.CommandTarget;
import io.github.sakurawald.core.command.argument.wrapper.impl.Dimension;
import io.github.sakurawald.module.initializer.ModuleInitializer;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LodestoneTrackerComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;

import java.util.Optional;
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

    private static void setTrackedTarget(ItemStack itemStack, ServerWorld world, BlockPos blockPos) {
        LodestoneTrackerComponent component = new LodestoneTrackerComponent(Optional.of(GlobalPos.create(world.getRegistryKey(), blockPos)), false);
        itemStack.set(DataComponentTypes.LODESTONE_TRACKER, component);
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
            setTrackedTarget(itemStack, target.getServerWorld(), target.getBlockPos());
            return CommandHelper.Return.SUCCESS;
        });
    }

    @CommandNode("reset")
    @Document("Let the compass in hand track nothing.")
    private static int reset(@CommandSource @CommandTarget ServerPlayerEntity player) {
        return withCompassInHand(player, (itemStack) -> {
            itemStack.set(DataComponentTypes.LODESTONE_TRACKER, null);
            return CommandHelper.Return.SUCCESS;
        });
    }
}
