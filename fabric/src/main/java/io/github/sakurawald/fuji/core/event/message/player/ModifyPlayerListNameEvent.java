package io.github.sakurawald.fuji.core.event.message.player;

import io.github.sakurawald.fuji.core.event.message.BaseEvent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
public class ModifyPlayerListNameEvent extends BaseEvent {

    @NotNull ServerPlayerEntity player;
    @Nullable Text text;

}
