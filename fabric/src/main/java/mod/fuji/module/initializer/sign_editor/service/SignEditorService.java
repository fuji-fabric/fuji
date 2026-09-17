package mod.fuji.module.initializer.sign_editor.service;

import java.util.Optional;
import java.util.function.Function;
import mod.fuji.core.auxiliary.minecraft.CommandHelper;
import mod.fuji.core.auxiliary.minecraft.EntityHelper;
import mod.fuji.core.auxiliary.minecraft.PlayerHelper;
import mod.fuji.core.auxiliary.minecraft.WorldHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.SignText;
import org.jetbrains.annotations.NotNull;

public class SignEditorService {

    public static final int MAX_SIGN_BLOCK_LINES = 4;

    public static int withSignBlockEntity(@NotNull ServerPlayer player, @NotNull BlockPos blockPos, @NotNull Function<SignBlockEntity, Integer> function) {
        ServerLevel level = PlayerHelper.getServerWorld(player);
        BlockEntity blockEntity = level.getBlockEntity(blockPos);
        if (!(blockEntity instanceof SignBlockEntity signBlockEntity)) {
            return CommandHelper.Return.FAILURE;
        }

        return function.apply(signBlockEntity);
    }

    public static void updateSignText(@NotNull ServerPlayer player, @NotNull SignBlockEntity signBlockEntity, Optional<Boolean> frontSide, Optional<Boolean> bothSides, @NotNull Function<SignText, SignText> mapper) {
        /* Apply the operation on both sides. */
        boolean $bothSides = bothSides.orElse(false);
        if ($bothSides) {
            EntityHelper.SignBlock.updateSignText(signBlockEntity, mapper::apply, true);
            EntityHelper.SignBlock.updateSignText(signBlockEntity, mapper::apply, false);
            return;
        }

        /* Apply the operation on the preferred side. */
        boolean preferredSide = frontSide.orElseGet(() -> EntityHelper.SignBlock.isFacingFront(player, signBlockEntity));
        EntityHelper.SignBlock.updateSignText(signBlockEntity, mapper::apply, preferredSide);
    }

    public static int selectLookingAtSignBlock(@NotNull ServerPlayer player, @NotNull Function<BlockPos, Integer> function) {
        BlockPos blockPos = WorldHelper.Raycast.getLookingAtBlockOrElseThrow(player);
        return function.apply(blockPos);
    }

    public static @NotNull SignText withGlowingState(@NotNull SignText signText, boolean value) {
        #if MC_VER < MC_26_3
        return signText.setHasGlowingText(value);
        #elif MC_VER >= MC_26_3
        return signText.withGlowingText(value);
        #endif
    }

    public static @NotNull SignText withLine(@NotNull SignText signText, int index, @NotNull Component text) {
        #if MC_VER < MC_26_3
        return signText.setMessage(index, text);
        #elif MC_VER >= MC_26_3
        return signText.asMutable().setLine(index, text).asImmutable();
        #endif
    }

    public static @NotNull SignText withColor(@NotNull SignText signText, @NotNull DyeColor color) {
        #if MC_VER < MC_26_3
        return signText.setColor(color);
        #elif MC_VER >= MC_26_3
        return signText.withColor(color);
        #endif
    }
}
