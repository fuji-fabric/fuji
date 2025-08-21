package io.github.sakurawald.fuji.module.initializer.jail.structure;

import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.core.structure.GlobalPos;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JailDescriptor {

    String id;
    String displayName = "<blue>The Great Jail</blue>";

    @Document(id = 1753684930648L, value = "Tae `jail duration` to use if you didn't specify a `jail duration`.")
    String defaultJailedDuration = "15m";
    boolean countRemainingJailSecondsWhenPrisonersOffline = false;

    GlobalPos globalPosition = new GlobalPos("minecraft:overworld", 0, 64, 0, 0, 0);

    Events events = new Events();
    @Data
    @NoArgsConstructor
    public static class Events {
        List<String> onJailedEvent = new ArrayList<>() {
            {
                this.add("send-broadcast <dark_red><b>The player %player:displayname% has been jailed.<newline>◉ Duration: %fuji:jail_specified_duration%<newline>◉ Reason: %fuji:jail_reason%");
                this.add("lp user %player:name% permission set group.jailed");
                this.add("when-online %player:name% send-title %player:name% --mainTitle \"<dark_red>You have been jailed.\"");
            }
        };
        List<String> onUnjailedEvent = new ArrayList<>() {
            {
                this.add("send-broadcast <green><b>The player %player:displayname% has been un-jailed.");
                this.add("lp user %player:name% permission unset group.jailed");
                this.add("when-online %player:name% send-title %player:name% --mainTitle \"<green>You have been un-jailed.\"");
            }
        };
    }

    Patrol patrol = new Patrol();
    @Data
    @NoArgsConstructor
    public static class Patrol {
        int patrolIntervalMillSeconds = 3 * 1000;
        List<String> patrolCommands = new ArrayList<>() {
            {
                this.add("execute as %player:name% at @s unless dimension %fuji:jail_dimension% run execute in %fuji:jail_dimension% run tp @s %fuji:jail_x% %fuji:jail_y% %fuji:jail_z%");
                this.add("execute as %player:name% if entity @s[x=%fuji:jail_x%,y=%fuji:jail_y%,z=%fuji:jail_z%,distance=8..] run tp @s %fuji:jail_x% %fuji:jail_y% %fuji:jail_z%");
            }
        };
    }

    public static JailDescriptor make(@NotNull String id, @NotNull GlobalPos globalPosition) {
        JailDescriptor jailDescriptor = new JailDescriptor();
        jailDescriptor.setId(id);
        jailDescriptor.setGlobalPosition(globalPosition);
        return jailDescriptor;
    }

}
