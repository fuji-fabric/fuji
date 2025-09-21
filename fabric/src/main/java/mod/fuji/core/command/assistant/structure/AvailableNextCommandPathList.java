package mod.fuji.core.command.assistant.structure;

import java.util.ArrayList;
import java.util.List;
import lombok.Value;

@Value
public class AvailableNextCommandPathList {
    List<AvailableNextCommandPath> entries = new ArrayList<>();
}
