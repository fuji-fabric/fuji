package io.github.sakurawald.fuji.module.initializer.command_attachment.config.model;

import io.github.sakurawald.fuji.module.initializer.command_attachment.structure.CommandAttachmentDataNode;
import java.util.concurrent.CopyOnWriteArrayList;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CommandAttachmentDataModel {

    CopyOnWriteArrayList<CommandAttachmentDataNode> nodes = new CopyOnWriteArrayList<>();

}
