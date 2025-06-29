package io.github.sakurawald.fuji.core.document.descriptor;


import net.minecraft.item.Item;
import net.minecraft.item.Items;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public class MetaDescriptor<T> extends StringDescriptor {

    public final @NotNull Function<String, ? extends T> valueTransformer;

    public MetaDescriptor(@NotNull String pattern, @NotNull Function<String, ? extends T> valueTransformer, @Nullable String document) {
        super(pattern, document);
        this.valueTransformer = valueTransformer;
    }

    public MetaDescriptor(boolean temporary, @NotNull String pattern, @NotNull Function<String, ? extends T> valueTransformer, @Nullable String document) {
        super(temporary, pattern, document);
        this.valueTransformer = valueTransformer;
    }

    @Override
    public Item toItem() {
        return Items.BAMBOO_DOOR;
    }

    @Override
    public int sortPriority() {
        return +1;
    }

    @Override
    public String getStringType() {
        return "Meta";
    }
}
