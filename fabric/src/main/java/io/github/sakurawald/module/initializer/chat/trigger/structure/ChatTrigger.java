package io.github.sakurawald.module.initializer.chat.trigger.structure;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class ChatTrigger {

    public String regex;
    public List<String> commands;
}
