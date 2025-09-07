package io.github.sakurawald.fuji.core.event.message.server.metadata;

import io.github.sakurawald.fuji.core.event.message.BaseEvent;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.minecraft.server.ServerMetadata;
import org.jetbrains.annotations.NotNull;

@Data
@EqualsAndHashCode(callSuper = true)
public class ModifyServerMetadataEvent extends BaseEvent {

    @NotNull ServerMetadata serverMetadata;

}
