package io.github.sakurawald.fuji.core.event.message.impl.on_demand.player;

import io.github.sakurawald.fuji.core.event.message.abst.BaseEvent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
public class ModifyPlayerDisplayNameEvent extends BaseEvent {

    @NotNull PlayerEntity player;
    @Nullable Text text;

}
