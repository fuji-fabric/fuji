package mod.fuji.core.command.argument.wrapper.impl;


import mod.fuji.core.command.argument.wrapper.abst.SingularValue;

import java.util.List;

public class GreedyStringList extends SingularValue<List<String>> {
    public GreedyStringList(List<String> value) {
        super(value);
    }
}
