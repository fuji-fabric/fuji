package io.github.sakurawald.fuji.module.initializer.disabler.max_player_disabler;

import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.core.event.annotation.EventConsumer;
import io.github.sakurawald.fuji.core.event.message.server.BypassPlayerLimitEvent;
import io.github.sakurawald.fuji.module.initializer.ModuleInitializer;

@Document(id = 1751975780821L, value = """
    This module disables the `max player limit` of the server.
    """)
public class MaxPlayerDisablerInitializer extends ModuleInitializer {

    @EventConsumer
    private static void disableMaxPlayerLimit(BypassPlayerLimitEvent event) {
        event.setCanBypass(true);
    }
}
