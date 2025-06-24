package io.github.sakurawald.module.initializer.command_toolbox.nickname.config.model;

import io.github.sakurawald.core.annotation.Document;

public class NicknameConfigModel {
    @Document("""
        The `format` used when `setting` the nickname.
        """)
    public String transform_nickname = "<blue>-</blue>%.12s";
}
