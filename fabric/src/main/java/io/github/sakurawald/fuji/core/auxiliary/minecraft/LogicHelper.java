package io.github.sakurawald.fuji.core.auxiliary.minecraft;

import net.minecraft.server.network.ServerPlayerEntity;

public class LogicHelper {

    public static void tryOrCancel(ServerPlayerEntity player, boolean shouldCancel, Runnable runnable) {
        if (shouldCancel) {
            TextHelper.sendMessageByKey(player, "operation.cancelled");
            return;
        }

        runnable.run();
    }

}
