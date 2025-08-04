package io.github.sakurawald.fuji.module.initializer.command_attachment.config.model;

import io.github.sakurawald.fuji.module.initializer.command_attachment.structure.CommandAttachmentDataNode;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CommandAttachmentDataModel {

    List<CommandAttachmentDataNode> nodes = new ArrayList<>();

}
