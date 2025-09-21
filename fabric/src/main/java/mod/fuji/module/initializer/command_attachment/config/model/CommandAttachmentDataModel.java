package mod.fuji.module.initializer.command_attachment.config.model;

import mod.fuji.module.initializer.command_attachment.structure.CommandAttachmentDataNode;
import java.util.concurrent.CopyOnWriteArrayList;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CommandAttachmentDataModel {

    CopyOnWriteArrayList<CommandAttachmentDataNode> nodes = new CopyOnWriteArrayList<>();

}
