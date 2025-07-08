package io.github.sakurawald.fuji.core.document.descriptor;

import net.minecraft.item.Item;
import net.minecraft.item.Items;
import org.jetbrains.annotations.NotNull;

public class PlaceholderDescriptor extends StringDescriptor{

    public PlaceholderDescriptor(@NotNull String pattern, long docStringId) {
        super(pattern, docStringId);
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
