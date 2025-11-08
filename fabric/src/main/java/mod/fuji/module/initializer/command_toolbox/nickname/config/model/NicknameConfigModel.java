package mod.fuji.module.initializer.command_toolbox.nickname.config.model;

import com.google.gson.annotations.SerializedName;
import lombok.Data;
import lombok.NoArgsConstructor;
import mod.fuji.core.config.constraint.StringConstraints;
import mod.fuji.core.document.annotation.Document;

@Data
@NoArgsConstructor
public class NicknameConfigModel {
    @Document(id = 1751825211243L, value = """
        The `format` used when `setting` the nickname.
        """)
    @SerializedName(value = "nickname_format", alternate = "transform_nickname")
    String nicknameFormat = "%s <grey>(%player:name%)";

    StringConstraints nicknameConstraints = new StringConstraints(16, ".+", "");
}
