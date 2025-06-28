package io.github.sakurawald.fuji.core.document.descriptor;

import net.minecraft.item.Item;
import net.minecraft.item.Items;

public class PermissionDescriptor extends StringDescriptor {

    public PermissionDescriptor(String pattern, String document) {
        super(pattern, document);
    }

    public PermissionDescriptor(boolean temporary, String pattern, String document) {
        super(temporary, pattern, document);
    }

    @Override
    public Item toItem() {
        return Items.CHERRY_DOOR;
    }

    @Override
    public int sortPriority() {
        return -1;
    }

    @Override
    public String getStringType() {
        return "Permission";
    }
}
