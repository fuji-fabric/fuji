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
            this.add(new SystemMessageRule(false, "Modify the style of player joined text.", false, "multiplayer.player.joined", "<green>[+] Player %s joined the server."));
            this.add(new SystemMessageRule(false, "Cancel the sending of player left text.", false, "multiplayer.player.left", null));

            /* Player death. */
            this.add(new SystemMessageRule(false, "Modify the player death message.", false, "death.attack.explosion.player", "<rainbow>%1$s booooooom because of %2$s"));
            this.add(new SystemMessageRule(false, "Modify the player death message.", false, "death.attack.fall", "<rb>%1$s hit the ground too hard"));
            this.add(new SystemMessageRule(false, "Modify the player death message.", false, "death.fell.accident.generic", "<rb>%1$s fell from a high place"));

            /* Vanilla Command Feedback */
            this.add(new SystemMessageRule(true, "Modify the style of `/seed` command feedback.", false, "commands.seed.success", "<rainbow>Seeeeeeeeeeed: %s"));

            /* Whitelist Screen */
            this.add(new SystemMessageRule(false, "Modify the text displaying in the whitelist screen.", true, "multiplayer.disconnect.not_whitelisted", "<rainbow>Please apply a whitelist first!"));

            /* Ban Screen */
            this.add(new SystemMessageRule(false, "Modify the text displaying in the ban screen.", true, "multiplayer.disconnect.banned", "<red><b><i>You are banned from this server"));
            this.add(new SystemMessageRule(false, "Modify the text displaying in the ban screen.", true, "multiplayer.disconnect.banned.reason", "<red><b><i>You are banned from this server<newline><yellow>Reason: %s"));

            /* Container Screen */
            this.add(new SystemMessageRule(false, "Modify the text displaying in chest screen.", true, "container.chest", "<rb>I see you opening the chest!"));

        }
    };

}
