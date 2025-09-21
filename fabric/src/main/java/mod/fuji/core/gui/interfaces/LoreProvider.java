package mod.fuji.core.gui.interfaces;

import java.util.List;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

public interface LoreProvider {
    List<Text> asLore(@NotNull ServerPlayerEntity player);
}
