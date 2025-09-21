package mod.fuji.module.initializer.back.structure;

import mod.fuji.core.auxiliary.CollectionUtil;
import mod.fuji.core.command.argument.wrapper.impl.Dimension;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public class LocationHistory {

    private List<LocationEntry> history = new ArrayList<>();

    public void pushEntry(LocationEntry entry) {
        this.history.add(entry);
        this.sortEntries();
    }

    public Iterator<LocationEntry> listEntries() {
        return this.history.iterator();
    }

    public @Nullable LocationEntry getLatestEntry() {
        return this.history.isEmpty() ? null : this.history.get(this.history.size() - 1);
    }

    public @Nullable LocationEntry findEntry(int lastNLocation, @Nullable Dimension targetDimension) {
        // filter the target list by dimension.
        List<LocationEntry> targetList = this.history
            .stream()
            .filter(it -> targetDimension == null
                || it.getLocation().sameLevel(targetDimension.getValue()))
            .collect(Collectors.toCollection(ArrayList::new));

        // find the target index.
        int index = targetList.size() - lastNLocation;
        if (!CollectionUtil.validIndex(index, targetList)) {
            return null;
        }

        return targetList.get(index);
    }

    public void clearEntries() {
        this.history.clear();
    }

    public void sortEntries() {
        Collections.sort(this.history);
    }

    public void trimEntries(int length) {
        if (this.history.size() < length) {
            return;
        }
        int start = this.history.size() - length;
        int end = this.history.size();
        this.history = this.history.subList(start, end);
    }

}
