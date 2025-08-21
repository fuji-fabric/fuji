package io.github.sakurawald.fuji.core.command.assistant.structure;

import lombok.Value;

@Value
public class AvailableNextCommandPath {
    String prefixString;
    String infixString;
    String suffixString;
}
