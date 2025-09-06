package io.github.sakurawald.fuji.core.event.message.impl.on_demand;

import io.github.sakurawald.fuji.core.event.message.abst.BaseEvent;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;

@EqualsAndHashCode(callSuper = true)
@Data
public class OnPlayerDeathEvent extends BaseEvent {
    @NotNull ServerPlayerEntity player;
}
