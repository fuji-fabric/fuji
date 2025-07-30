package io.github.sakurawald.fuji.module.initializer.warning.structure;

import com.google.gson.annotations.SerializedName;
import io.github.sakurawald.fuji.core.auxiliary.ChronosUtil;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import java.util.List;
import lombok.Data;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

@Data
public class Warning {

    public long createdTimestamp;
    @SerializedName(value = "creatorName", alternate = "createdByPlayer")
    public String creatorName;
    public String description;

    public static Warning makeWarning(String creatorName, String description) {
        Warning warning = new Warning();
        warning.createdTimestamp = System.currentTimeMillis();
        warning.creatorName = creatorName;
        warning.description = description;
        return warning;
    }

    @NotNull
    public List<Text> asLore(Object audience) {
        return List.of(
            TextHelper.getTextByKey(audience, "entity.created_by_player", creatorName)
            , TextHelper.getTextByKey(audience, "entity.created_timestamp", ChronosUtil.formatDate(createdTimestamp))
            , TextHelper.getTextByKey(audience, "entity.description", description)
        );
    }
}
