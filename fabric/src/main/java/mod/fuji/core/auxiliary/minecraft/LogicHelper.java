package mod.fuji.core.auxiliary.minecraft;

import net.minecraft.server.network.ServerPlayerEntity;

public class LogicHelper {

    public static void withCancelCheck(ServerPlayerEntity player, boolean shouldCancel, Runnable runnable) {
        if (shouldCancel) {
            TextHelper.sendTextByKey(player, "operation.cancelled");
            return;
        }

        runnable.run();
    }

}
