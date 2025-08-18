package io.github.sakurawald.fuji.core.command.assistant.structure;

import lombok.Data;

@Data
public class AvailableNextCommandPath {
    final String prefixString;
    final String infixString;
    final String suffixString;
}
