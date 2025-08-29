package io.github.sakurawald.fuji.module.initializer.command_attachment.structure;

import com.google.gson.annotations.SerializedName;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CommandAttachmentDataNode {

    String id;

    @SerializedName(value = "attachments", alternate = "model")
    CommandAttachments attachments = new CommandAttachments();
}
