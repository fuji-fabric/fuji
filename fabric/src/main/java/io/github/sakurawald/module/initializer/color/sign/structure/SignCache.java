package io.github.sakurawald.module.initializer.color.sign.structure;

import lombok.Data;
import lombok.With;

import java.util.List;

@Data
@With
public class SignCache {
    final List<String> frontLines;
    final List<String> backLines;
}
