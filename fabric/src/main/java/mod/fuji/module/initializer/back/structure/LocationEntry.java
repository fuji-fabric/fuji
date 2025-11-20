package mod.fuji.module.initializer.back.structure;

import mod.fuji.core.auxiliary.ChronosUtil;
import mod.fuji.core.structure.GlobalPos;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LocationEntry implements Comparable<LocationEntry> {
    GlobalPos location;
    Long savedTimestamp;

    public static LocationEntry makeLocationEntry(@NotNull ServerPlayer player) {
        GlobalPos location = GlobalPos.of(player);
        Long saved_timestamp = ChronosUtil.getCurrentTimestamp();
        return new LocationEntry(location, saved_timestamp);
    }

    @Override
    public int compareTo(@NotNull LocationEntry that) {
        return Comparator.comparingLong(LocationEntry::getSavedTimestamp)
            .compare(this, that);
    }
}
