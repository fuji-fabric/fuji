package mod.fuji.module.initializer.disabler.max_player_disabler;

import mod.fuji.core.document.annotation.Document;
import mod.fuji.core.event.annotation.EventConsumer;
import mod.fuji.core.event.message.server.BypassPlayerLimitEvent;
import mod.fuji.module.initializer.ModuleInitializer;

@Document(id = 1751975780821L, value = """
    This module disables the `max player limit` of the server.
    """)
public class MaxPlayerDisablerInitializer extends ModuleInitializer {

    @EventConsumer(injectorPriority = EventConsumer.LOWEST)
    private static void disableMaxPlayerLimit(BypassPlayerLimitEvent event) {
        event.setCanBypass(true);
    }
}
