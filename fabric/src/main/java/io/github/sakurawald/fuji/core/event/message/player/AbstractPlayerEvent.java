package io.github.sakurawald.fuji.core.event.message.player;

import io.github.sakurawald.fuji.core.event.message.BaseEvent;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;

@EqualsAndHashCode(callSuper = true)
@Data
public class AbstractPlayerEvent extends BaseEvent {

    @NotNull ServerPlayerEntity player;
}
