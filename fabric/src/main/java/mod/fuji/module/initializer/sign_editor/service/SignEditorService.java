package mod.fuji.module.initializer.sign_editor.service;

import java.util.Optional;
import java.util.function.Function;
import mod.fuji.core.auxiliary.minecraft.CommandHelper;
import mod.fuji.core.auxiliary.minecraft.PlayerHelper;
import mod.fuji.core.auxiliary.minecraft.WorldHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
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
            signBlockEntity.updateText(mapper::apply, true);
            signBlockEntity.updateText(mapper::apply, false);
            return;
        }

        /* Apply the operation on the prefer side. */
        boolean isPlayerFacingFront = frontSide.orElseGet(() -> signBlockEntity.isFacingFrontText(player));
        signBlockEntity.updateText(mapper::apply, isPlayerFacingFront);
    }

    public static int selectLookingAtSignBlock(@NotNull ServerPlayer player, @NotNull Function<BlockPos, Integer> function) {
        BlockPos blockPos = WorldHelper.Raycast.getLookingAtBlockOrElseThrow(player);
        return function.apply(blockPos);
    }
}
