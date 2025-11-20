package mod.fuji.core.gui.interfaces;

import java.util.List;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public interface LoreProvider {
    List<Component> asLore(@NotNull ServerPlayer player);
}
