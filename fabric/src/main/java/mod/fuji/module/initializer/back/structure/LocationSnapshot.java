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
public class LocationSnapshot implements Comparable<LocationSnapshot> {

    GlobalPos location;
    long savedTimestamp;

    public static @NotNull LocationSnapshot ofPlayer(@NotNull ServerPlayer player) {
        GlobalPos location = GlobalPos.of(player);
        long savedTimestamp = ChronosUtil.getCurrentTimestamp();
        return new LocationSnapshot(location, savedTimestamp);
    }

    @Override
    public int compareTo(@NotNull LocationSnapshot that) {
        return Comparator
            .comparingLong(LocationSnapshot::getSavedTimestamp)
            .compare(this, that);
    }

}
