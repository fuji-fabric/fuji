package io.github.sakurawald.module.initializer.chat.style.model;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public class ChatFormatModel {

    public @NotNull Format format = new Format();

    public static class Format {
        public @NotNull HashMap<String, String> player2format = new HashMap<>() {
            {
                this.put("Steve", "<#FFC7EA>%message%");
            }
        };
    }
}
