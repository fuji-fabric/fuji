package mod.fuji.core.command.argument.wrapper.impl;

import mod.fuji.core.command.argument.wrapper.abst.SingularValue;

import java.util.List;

public class StringList extends SingularValue<List<String>> {
    public StringList(List<String> value) {
        super(value);
    }
}
