package io.github.sakurawald.fuji.core.event.impl.on_demand;

import io.github.sakurawald.fuji.core.event.abst.BaseEvent;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.minecraft.server.ServerMetadata;
import org.jetbrains.annotations.NotNull;

@Data
@EqualsAndHashCode(callSuper = true)
public class ModifyServerMetadataEvent extends BaseEvent {

    @NotNull ServerMetadata serverMetadata;

}
