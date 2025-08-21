package io.github.sakurawald.fuji.module.initializer.color.sign.structure;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@With
public class SignCache {
    List<String> frontLines;
    List<String> backLines;
}
