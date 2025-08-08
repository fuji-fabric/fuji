package io.github.sakurawald.fuji.module.initializer.warning.structure;

import com.google.gson.annotations.SerializedName;
import io.github.sakurawald.fuji.core.auxiliary.ChronosUtil;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Data
@NoArgsConstructor
public class Warning {

    long createdTimestamp;

    @Nullable Long expirationTimestamp;

    @SerializedName(value = "creator_name", alternate = {"createdByPlayer", "creatorName"})
    String creatorName;

    String description;

    public static Warning make(@NotNull String creatorName, @NotNull String description, @Nullable Long expirationTimestamp) {
        Warning entity = new Warning();
        entity.createdTimestamp = System.currentTimeMillis();
        entity.creatorName = creatorName;
        entity.description = description;
        entity.expirationTimestamp = expirationTimestamp;
        return entity;
    }

    public @NotNull List<Text> asLore(Object audience) {
        return List.of(
            TextHelper.getTextByKey(audience, "entity.created_by_player", creatorName)
            , TextHelper.getTextByKey(audience, "entity.created_timestamp", ChronosUtil.Formatter.formatDate(createdTimestamp))
            , TextHelper.getTextByKey(audience, "entity.expiration_timestamp", ChronosUtil.Formatter.formatDate(expirationTimestamp))
            , TextHelper.getTextByKey(audience, "entity.description", description)
        );
    }

    public @NotNull Item asItem() {
        return this.getExpirationTimestamp() == null ? Items.PAPER : Items.MAP;
    }
}
