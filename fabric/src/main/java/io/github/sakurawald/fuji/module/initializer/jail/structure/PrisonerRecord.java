package io.github.sakurawald.fuji.module.initializer.jail.structure;

import io.github.sakurawald.fuji.core.document.annotation.Document;
import lombok.Data;

@Data
public class PrisonerRecord {

    @Document(id = 1753685346716L, value = "The `jailed player name`.")
    String playerName;

    @Document(id = 1753685321191L, value = "The player name who created this `jail record`.")
    String creatorName;
    long createdTimestamp;
    String reason;

}
