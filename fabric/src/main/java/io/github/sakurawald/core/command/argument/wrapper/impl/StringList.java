package io.github.sakurawald.core.command.argument.wrapper.impl;

import io.github.sakurawald.core.command.argument.wrapper.abst.SingularValue;

import java.util.List;

public class StringList extends SingularValue<List<String>> {
    public StringList(List<String> value) {
        super(value);
    }
}
