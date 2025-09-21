package mod.fuji.module.initializer.command_state.structure;

import mod.fuji.core.document.annotation.Document;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StateDescriptor {

    boolean enable;

    String id;

    @Document(id = 1756693185168L, value = """
        The `predicate commands` used to define this `state`.
        """)
    Definition definition = new Definition();

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Definition {
        List<String> predicateCommands = new ArrayList<>();
    }

    @Document(id = 1756693210057L, value = """
        The intervals to `check` and `update` the `status` of this `state` for online players.
        """)
    int updateIntervalSeconds = 3;

    Events events = new Events();

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Events {
        List<String> onEnterThisStateCommands = new ArrayList<>();

        List<String> onLeaveThisStateCommands = new ArrayList<>();
    }

}
