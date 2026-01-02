package mod.fuji.module.initializer.back.structure;

import java.util.Optional;
import lombok.Data;
import lombok.NoArgsConstructor;
import mod.fuji.core.auxiliary.CollectionUtil;
import mod.fuji.core.command.argument.wrapper.impl.Dimension;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

@Data
@NoArgsConstructor
public class LocationHistory {

    List<LocationSnapshot> history = new ArrayList<>();

    public void pushLocationSnapshot(@NotNull LocationSnapshot snapshot) {
        this.history.add(snapshot);
        this.sortHistory();
    }

    public @NotNull Iterator<LocationSnapshot> listLocationSnapshots() {
        return this.history.iterator();
    }

    public Optional<LocationSnapshot> getLastLocationSnapshot() {
        return CollectionUtil.lastElement(this.history);
    }

    public Optional<LocationSnapshot> findLocationSnapshot(int lastNLocation, @Nullable Dimension targetDimension) {
        /* Filter the list by dimension. */
        List<LocationSnapshot> result = this.history
            .stream()
            .filter(it -> targetDimension == null
                || it.getLocation().sameLevel(targetDimension.getValue()))
            .toList();

        /* Filter the list by ordinal. */
        int index = result.size() - lastNLocation;
        if (!CollectionUtil.validIndex(index, result)) {
            return Optional.empty();
        }

        return Optional.ofNullable(result.get(index));
    }

    public void clearHistory() {
        this.history.clear();
    }

    public void sortHistory() {
        Collections.sort(this.history);
    }

    public void trimHistory(int maxSize) {
        if (this.history.size() < maxSize) {
            return;
        }

        int start = this.history.size() - maxSize;
        int end = this.history.size();
        this.history = this.history.subList(start, end);
    }

}
