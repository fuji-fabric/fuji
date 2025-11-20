package mod.fuji.module.initializer.title.structure;

import mod.fuji.core.auxiliary.minecraft.ItemStackHelper;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

@Data
@NoArgsConstructor
public class TitleDescriptor {

    String id;
    String item;
    String displayName;
    List<String> lore;

    public @NotNull ItemStack toItemStack() {
        return ItemStackHelper.Parser.parseItemStack(this.item);
    }

}
