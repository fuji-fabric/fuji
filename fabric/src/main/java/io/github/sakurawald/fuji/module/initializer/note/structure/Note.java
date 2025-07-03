package io.github.sakurawald.fuji.module.initializer.note.structure;

import io.github.sakurawald.fuji.core.auxiliary.ChronosUtil;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.PlayerHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import java.util.List;
import lombok.Data;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

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

    @NotNull
    public List<Text> asLore(Object audience) {
        return List.of(
            TextHelper.getTextByKey(audience, "entity.created_by_player", createdByPlayer)
            , TextHelper.getTextByKey(audience, "entity.created_timestamp", ChronosUtil.toDefaultDateFormat(createdTimestamp))
            , TextHelper.getTextByKey(audience, "entity.description", description)
        );
    }
}
