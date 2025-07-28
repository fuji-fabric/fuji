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

    Events events = new Events();
    @Data
    public static class Events {
        List<String> onJailedEvent = new ArrayList<>();
        List<String> onUnjaildEvent = new ArrayList<>();
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
