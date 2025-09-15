package io.github.sakurawald.fuji.module.initializer.system_message.config.model;


import io.github.sakurawald.fuji.module.initializer.system_message.structure.SystemMessageRule;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class SystemMessageConfigModel {

    List<SystemMessageRule> rules = new ArrayList<>() {
        {
            /* Join and leave. */
            this.add(new SystemMessageRule(true, "Modify the style of player joined text.","multiplayer.player.joined", "<green>Player %s joined the server."));
            this.add(new SystemMessageRule(true, "Cancel the sending of player left text.", "multiplayer.player.left", null));

            /* Player death. */
            this.add(new SystemMessageRule(true,"Modify the player death message.","death.attack.explosion.player","<rainbow>%1$s booooooom because of %2$s"));

            /* Vanilla Command Feedback. */
            this.add(new SystemMessageRule(true, "Modify the style of `/seed` command feedback.", "commands.seed.success", "<rainbow>Seeeeeeeeeeed: %s"));

            /* Screen. */
            this.add(new SystemMessageRule(true, "Modify the text displaying in the whitelist screen.","multiplayer.disconnect.not_whitelisted", "<rainbow>Please apply a whitelist first!"));

            this.add(new SystemMessageRule(true, "Modify the text displaying in chest screen.","container.chest", "<rb>I see you opening the chest!"));

            this.add(new SystemMessageRule(true, "Modify the text displaying in the server closed screen", "multiplayer.disconnect.server_shutdown", "<red>Server closeeeeeeeed"));

            this.add(new SystemMessageRule(true, "Modify the style of `/ban` command feedback.", "multiplayer.disconnect.banned", "<red>You are banned from this server"));
            this.add(new SystemMessageRule(true, "Modify the style of `/ban` command feedback.", "multiplayer.disconnect.banned.reason", "<red>You are banned from this server<newline><yellow>Reason: %s"));

        }
    };

}
