package io.github.sakurawald.fuji.core.auxiliary.minecraft;

import net.minecraft.server.network.ServerPlayerEntity;

public class LogicHelper {

    public static void tryOrCancel(ServerPlayerEntity player, boolean shouldCancel, Runnable runnable) {
        if (shouldCancel) {
            TextHelper.sendTextByKey(player, "operation.cancelled");
            return;
        }

        runnable.run();
    }

    public static void tryOperateOnThisEntity(ServerPlayerEntity player, boolean canOperate, Runnable runnable) {
        if (!canOperate) {
            TextHelper.sendActionBarByKey(player, "works.work.set.no_perm");
            return;
        }

        runnable.run();
    }

}
