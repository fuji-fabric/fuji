package io.github.sakurawald.fuji.module.initializer.note.structure;

import com.google.gson.annotations.SerializedName;
import io.github.sakurawald.fuji.core.auxiliary.ChronosUtil;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import java.util.List;
import lombok.Data;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

@Data
public class Note {

    public long createdTimestamp;
    @SerializedName(value = "creatorName", alternate = "createdByPlayer")
    public String creatorName;
    public String description;

    public static Note makeNote(String creatorName, String description) {
        Note note = new Note();
        note.createdTimestamp = System.currentTimeMillis();
        note.creatorName = creatorName;
        note.description = description;
        return note;
    }

    @NotNull
    public List<Text> asLore(Object audience) {
        return List.of(
            TextHelper.getTextByKey(audience, "entity.created_by_player", creatorName)
            , TextHelper.getTextByKey(audience, "entity.created_timestamp", ChronosUtil.toDefaultDateFormat(createdTimestamp))
            , TextHelper.getTextByKey(audience, "entity.description", description)
        );
    }
}
