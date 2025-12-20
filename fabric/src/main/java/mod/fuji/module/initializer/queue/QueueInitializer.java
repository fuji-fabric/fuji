package mod.fuji.module.initializer.queue;

import mod.fuji.core.auxiliary.minecraft.AuthlibHelper;
import mod.fuji.core.auxiliary.minecraft.LuckpermsHelper;
import mod.fuji.core.document.annotation.Document;
import mod.fuji.core.document.descriptor.PermissionDescriptor;
import mod.fuji.core.event.annotation.EventConsumer;
import mod.fuji.core.event.message.server.BypassPlayerLimitEvent;
import mod.fuji.module.initializer.ModuleInitializer;
import java.util.UUID;
import net.luckperms.api.util.Tristate;
import org.jetbrains.annotations.NotNull;

@Document(id = 1757757045008L, value = """
    This module provides customization of join queue.
    """)
public class QueueInitializer extends ModuleInitializer {

    @Document(id = 1757757163621L, value = """
        To bypass the `player limit`.
        """)
    private static final PermissionDescriptor BYPASS_PLAYER_LIMIT_PERMISSION = new PermissionDescriptor("fuji.queue.bypass.player_limit", 1757757163621L);

    @EventConsumer(injectorPriority = EventConsumer.HIGHEST)
    private static void consumeBypassPlayerLimitEvent(BypassPlayerLimitEvent event) {
        @NotNull UUID id = AuthlibHelper.getGameProfileId(event.getGameProfile());
        @NotNull Tristate permissionResult = LuckpermsHelper.getPermission(id, BYPASS_PLAYER_LIMIT_PERMISSION);
        if (!permissionResult.equals(Tristate.UNDEFINED)) {
            event.setCanBypass(permissionResult.asBoolean());
        }
    }


}
