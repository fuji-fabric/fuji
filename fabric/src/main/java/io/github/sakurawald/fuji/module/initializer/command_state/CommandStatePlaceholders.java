package io.github.sakurawald.fuji.module.initializer.command_state;

import io.github.sakurawald.fuji.core.auxiliary.minecraft.PlaceholderHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.fuji.core.document.annotation.DocStringProvider;
import io.github.sakurawald.fuji.core.document.descriptor.PlaceholderDescriptor;
import io.github.sakurawald.fuji.module.initializer.command_state.service.CommandStateService;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import net.minecraft.text.Text;

public class CommandStatePlaceholders {

    @DocStringProvider(id = 1756709272938L, value = """
        Returns the value of specified `state` of the player.

        Example:
        - `%fuji:is_in_state is-in-overworld%`
        """)
    public static void registerIsInStatePlaceholder() {
        PlaceholderDescriptor descriptor = new PlaceholderDescriptor("is_in_state", 1756709272938L);
        PlaceholderHelper.registerPlayerPlaceholder(descriptor, (player, args) -> {
            @SuppressWarnings("UnnecessaryLocalVariable") String stateId = args;

            return CommandStateService
                .findStateDescriptor(stateId)
                .map(stateDescriptor -> {
                    AtomicReference<Text> textRef = new AtomicReference<>();

                    CommandStateService.withPlayerStateMap(player, playerStates -> {
                        Text text = Optional
                            .ofNullable(playerStates.getStateMap().get(stateId))
                            .map(it -> Text.of(String.valueOf(it.getValue())))
                            .orElseGet(() -> TextHelper.getTextByKey(player, "command_state.state.no_value"));
                        textRef.set(text);
                    });

                    return textRef.get();
                })
                .orElseGet(() -> TextHelper.getTextByKey(player, "command_state.state.not_found", stateId));
        });
    }

}
