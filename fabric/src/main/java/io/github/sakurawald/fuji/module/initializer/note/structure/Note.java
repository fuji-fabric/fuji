package io.github.sakurawald.fuji.module.initializer.note.structure;

import lombok.Data;

@Data
public class Note {

    public long createdTimestamp;
    public String createdByPlayer;

    public String reason;

}
