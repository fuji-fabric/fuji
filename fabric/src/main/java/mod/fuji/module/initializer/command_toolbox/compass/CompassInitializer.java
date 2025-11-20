package mod.fuji.module.initializer.command_toolbox.compass;

import mod.fuji.core.document.annotation.Document;
import mod.fuji.core.auxiliary.minecraft.CommandHelper;
import mod.fuji.core.auxiliary.minecraft.EntityHelper;
import mod.fuji.core.auxiliary.minecraft.TextHelper;
import mod.fuji.core.command.annotation.CommandNode;
import mod.fuji.core.command.annotation.CommandSource;
import mod.fuji.core.command.annotation.CommandTarget;
import mod.fuji.core.command.argument.wrapper.impl.Dimension;
import mod.fuji.module.initializer.ModuleInitializer;
#if MC_VER <= MC_1_20_4
import mod.fuji.core.auxiliary.minecraft.RegistryHelper;
import mod.fuji.core.auxiliary.minecraft.ItemStackHelper;
#elif MC_VER > MC_1_20_4
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.LodestoneTracker;
import net.minecraft.core.GlobalPos;
import java.util.Optional;
#endif

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

@Document(id = 1751825174228L, value = """
    Allow you to set the target point of a compass item.
    """)
@CommandNode("compass")
public class CompassInitializer extends ModuleInitializer {

    private static int withCompassInHand(ServerPlayer source, Function<ItemStack, Integer> function) {
        ItemStack itemStack = source.getMainHandItem();
        if (!itemStack.getItem().equals(Items.COMPASS)) {
            TextHelper.sendTextByKey(source, "compass.no_compass");
            return CommandHelper.Return.FAILURE;
        }

        return function.apply(itemStack);
    }

    private static void setTrackedTarget(ItemStack itemStack, @Nullable ServerLevel world, @Nullable BlockPos blockPos) {

        #if MC_VER <= MC_1_20_4
        ItemStackHelper.CustomData.withCustomDataNbt(itemStack, tag -> {
            if (world == null) {
                tag.remove("LodestoneTracked");
                tag.remove("LodestoneDimension");
            } else {
                tag.putBoolean("LodestoneTracked", false);
                tag.putString("LodestoneDimension", RegistryHelper.getIdAsString(world));
            }

            if (blockPos == null) {
                tag.remove("LodestoneTracked");
                tag.remove("LodestonePos");
            } else {
                tag.putBoolean("LodestoneTracked", false);
                net.minecraft.nbt.CompoundTag posTag = new net.minecraft.nbt.CompoundTag();
                posTag.putInt("X", blockPos.getX());
                posTag.putInt("Y", blockPos.getY());
                posTag.putInt("Z", blockPos.getZ());
                tag.put("LodestonePos", posTag);
            }

        });

        #elif MC_VER > MC_1_20_4
        LodestoneTracker component = new LodestoneTracker(Optional.of(GlobalPos.of(world.dimension(), blockPos)), false);
        itemStack.set(DataComponents.LODESTONE_TRACKER, component);
        #endif

    }

    @Document(id = 1751825179946L, value = "Let the compass in hand track a specified position.")
    @CommandNode("track pos")
    private static int $track(@CommandSource @CommandTarget ServerPlayer player, Dimension dimension, BlockPos blockPos) {
        return withCompassInHand(player, (itemStack) -> {
            setTrackedTarget(itemStack, dimension.getValue(), blockPos);
            return CommandHelper.Return.SUCCESS;
        });
    }

    @Document(id = 1751825185305L, value = "Let the compass in hand track a specified player.")
    @CommandNode("track player")
    private static int $track(@CommandSource @CommandTarget ServerPlayer player, ServerPlayer target) {
        return withCompassInHand(player, (itemStack) -> {
            setTrackedTarget(itemStack, EntityHelper.getServerWorld(target), target.blockPosition());
            return CommandHelper.Return.SUCCESS;
        });
    }

    @Document(id = 1751825191491L, value = "Let the compass in hand track nothing.")
    @CommandNode("reset")
    private static int $reset(@CommandSource @CommandTarget ServerPlayer player) {
        return withCompassInHand(player, (itemStack) -> {
            setTrackedTarget(itemStack,null,null);
            return CommandHelper.Return.SUCCESS;
        });
    }
}
