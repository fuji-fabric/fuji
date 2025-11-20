package mod.fuji.core.auxiliary.minecraft;

import net.minecraft.server.level.ServerPlayer;

public class LogicHelper {

    public static void withCancelCheck(ServerPlayer player, boolean shouldCancel, Runnable runnable) {
        if (shouldCancel) {
            TextHelper.sendTextByKey(player, "operation.cancelled");
            return;
        }

        runnable.run();
    }

}
