package io.github.sakurawald.fuji.module.initializer.back.structure;

import io.github.sakurawald.fuji.core.auxiliary.ChronosUtil;
import io.github.sakurawald.fuji.core.structure.GlobalPos;
import lombok.AllArgsConstructor;
import lombok.Data;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;

@Data
@AllArgsConstructor
public class LocationEntry implements Comparable<LocationEntry> {
    GlobalPos location;
    Long savedTimestamp;

    public static LocationEntry makeLocationEntry(@NotNull ServerPlayerEntity player) {
        GlobalPos location = GlobalPos.of(player);
        Long saved_timestamp = ChronosUtil.getCurrentMillis();
        return new LocationEntry(location, saved_timestamp);
    }

    @Override
    public int compareTo(@NotNull LocationEntry that) {
        return Comparator.comparingLong(LocationEntry::getSavedTimestamp)
            .compare(this, that);
    }
}
