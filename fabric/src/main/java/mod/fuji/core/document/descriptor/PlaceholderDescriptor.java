package mod.fuji.core.document.descriptor;

import mod.fuji.Fuji;
import java.util.List;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;

public class PlaceholderDescriptor extends StringDescriptor{

    public PlaceholderDescriptor(@NotNull String pattern, long docStringId) {
        super(pattern, docStringId);
    }

    public static @NotNull List<StringDescriptor> getPlaceholderDescriptors() {
        return REGISTERED_STRING_DESCRIPTORS
            .stream()
            .filter(it -> it instanceof PlaceholderDescriptor)
            .toList();
    }

    @Override
    public String getStringType() {
        return "Placeholder";
    }

    @Override
    public Item toItem() {
        return Items.NAME_TAG;
    }

    @Override
    public @NotNull String toNameString() {
        return Fuji.MOD_ID + ":" + this.getString();
    }
}
