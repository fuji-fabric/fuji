package io.github.sakurawald.fuji.core.event.impl.on_demand;

import io.github.sakurawald.fuji.core.event.abst.BaseEvent;
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
