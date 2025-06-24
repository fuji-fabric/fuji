package io.github.sakurawald.core.structure.descriptor;

import net.minecraft.item.Item;
import net.minecraft.item.Items;

public class MetaDescriptor extends StringDescriptor{

    public MetaDescriptor(String pattern, String document) {
        super(pattern, document);
    }

    public MetaDescriptor(boolean temporary, String pattern, String document) {
        super(temporary, pattern, document);
    }

    @Override
    public Item toItem() {
        return Items.BAMBOO_DOOR;
    }
}
