package io.github.sakurawald.fuji.core.command.assistant.structure;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class AvailableNextCommandPathList {
    List<AvailableNextCommandPath> entries = new ArrayList<>();
}
