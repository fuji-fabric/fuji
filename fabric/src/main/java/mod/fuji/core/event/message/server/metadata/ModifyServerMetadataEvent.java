package mod.fuji.core.event.message.server.metadata;

import mod.fuji.core.event.message.BaseEvent;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.minecraft.network.protocol.status.ServerStatus;
import org.jetbrains.annotations.NotNull;

@Data
@EqualsAndHashCode(callSuper = true)
public class ModifyServerMetadataEvent extends BaseEvent {

    @NotNull ServerStatus serverMetadata;

}
