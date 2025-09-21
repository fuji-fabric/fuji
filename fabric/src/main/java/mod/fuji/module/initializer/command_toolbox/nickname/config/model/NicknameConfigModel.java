package mod.fuji.module.initializer.command_toolbox.nickname.config.model;

import com.google.gson.annotations.SerializedName;
import mod.fuji.core.document.annotation.Document;

public class NicknameConfigModel {
    @Document(id = 1751825211243L, value = """
        The `format` used when `setting` the nickname.
        """)
    @SerializedName(value = "nickname_format", alternate = "transform_nickname")
    public String nicknameFormat = "%.12s <grey>(%player:name%)";
}
