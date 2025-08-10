package io.github.sakurawald.fuji.core.command.argument.wrapper.abst;

import lombok.Data;

@Data
public class SingularValue<T> {
    final T value;

    @Override
    public String toString() {
        return this.value.toString();
    }

}
