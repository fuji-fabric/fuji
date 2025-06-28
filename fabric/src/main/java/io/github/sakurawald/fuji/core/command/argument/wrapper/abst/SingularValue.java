package io.github.sakurawald.fuji.core.command.argument.wrapper.abst;

import lombok.Data;

@SuppressWarnings("unused")
@Data
public class SingularValue<T> {
    final T value;
}
