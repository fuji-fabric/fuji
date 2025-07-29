package io.github.sakurawald.fuji.module.initializer.jail.structure;

import io.github.sakurawald.fuji.core.document.annotation.Document;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JailDescriptor {

    String id;
    String displayName = "<dark_red>Jail";

    @Document(id = 1753684930648L, value = "Tae `jail duration` to use if you didn't specify a `jail duration`.")
    String defaultJailedDuration = "30m";
    boolean countRemainingJailSecondsWhenOffline = false;

    Events events = new Events();
    @Data
    public static class Events {
        List<String> onJailedEvent = new ArrayList<>() {
            {
                this.add("send-broadcast <dark_red>The player %player:displayname% has been jailed.");
                this.add("lp user %player:name% permission set group.jailed");
                this.add("when-online %player:name% send-message %player:name% <dark_red>You have been jailed.");
            }
        };
        List<String> onUnjailedEvent = new ArrayList<>() {
            {
                this.add("send-broadcast <green>The player %player:displayname% has been un-jailed.");
                this.add("lp user %player:name% permission unset group.jailed");
                this.add("when-online %player:name% send-message %player:name% <green>You have been un-jailed.");
            }
        };
    }

    Patrol patrol = new Patrol();
    @Data
    public static class Patrol {
        int patrolIntervalTicks = 20 * 10;
        List<String> patrolCommands = new ArrayList<>();
    }

    public static JailDescriptor make(String id) {
        JailDescriptor jailDescriptor = new JailDescriptor();
        jailDescriptor.setId(id);
        return jailDescriptor;
    }

}
