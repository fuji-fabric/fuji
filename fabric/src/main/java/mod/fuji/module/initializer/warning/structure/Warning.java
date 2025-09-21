package mod.fuji.module.initializer.warning.structure;

import com.google.gson.annotations.SerializedName;
import mod.fuji.core.auxiliary.ChronosUtil;
import mod.fuji.core.auxiliary.minecraft.GuiHelper;
import mod.fuji.core.auxiliary.minecraft.TextHelper;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.minecraft.item.Item;
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
            TextHelper.getTextByKey(audience, "entity.active", isActive())
            , TextHelper.getTextByKey(audience, "entity.created_by_player", creatorName)
            , TextHelper.getTextByKey(audience, "entity.created_timestamp", ChronosUtil.Formatter.formatDate(createdTimestamp))
            , TextHelper.getTextByKey(audience, "entity.expiration_timestamp", ChronosUtil.Formatter.formatDate(expirationTimestamp))
            , TextHelper.getTextByKey(audience, "entity.description", description)
        );
    }

    public boolean isTemporalWarning() {
        return !isPermanentWarning();
    }

    public boolean isPermanentWarning() {
        return this.getExpirationTimestamp() == null;
    }

    public boolean isActive() {
        if (this.getExpirationTimestamp() == null) {
            return true;
        }

        return System.currentTimeMillis() < this.getExpirationTimestamp();
    }

    public @NotNull Item asItem() {
        return GuiHelper.Material.fromBooleanValue(this.isActive());
    }
}
