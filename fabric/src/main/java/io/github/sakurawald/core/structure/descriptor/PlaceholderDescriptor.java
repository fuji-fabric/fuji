package io.github.sakurawald.core.structure.descriptor;

import net.minecraft.item.Item;
import net.minecraft.item.Items;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PlaceholderDescriptor extends StringDescriptor{

    public PlaceholderDescriptor(@NotNull String pattern, @Nullable String document) {
        super(pattern, document);
    }

    @Override
    public String getStringType() {
        return "Placeholder";
    }

    @Override
    public Item toItem() {
        return Items.NAME_TAG;
    }
}
