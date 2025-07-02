package io.github.sakurawald.fuji.module.initializer.note.structure;

import io.github.sakurawald.fuji.core.auxiliary.minecraft.PlayerHelper;
import lombok.Data;
import net.minecraft.server.network.ServerPlayerEntity;

@Data
public class Note {

    public long createdTimestamp;
    public String createdByPlayer;
    public String description;

    public static Note makeNote(ServerPlayerEntity createdByPlayer, String description) {
        Note note = new Note();
        note.createdTimestamp = System.currentTimeMillis();
        note.createdByPlayer = PlayerHelper.getPlayerName(createdByPlayer);
        note.description = description;
        return note;
    }

}
