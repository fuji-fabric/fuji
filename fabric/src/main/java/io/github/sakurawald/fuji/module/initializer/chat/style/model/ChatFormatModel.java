package io.github.sakurawald.fuji.module.initializer.chat.style.model;

import io.github.sakurawald.fuji.core.document.annotation.Document;

import java.util.HashMap;

public class ChatFormatModel {

    @Document("""
        Per-player chat content format.
        """)
    public Format format = new Format();
    public static class Format {
        public HashMap<String, String> player2format = new HashMap<>() {
            {
                this.put("Steve", "<#FFC7EA>%message%");
            }
        };
    }
}
