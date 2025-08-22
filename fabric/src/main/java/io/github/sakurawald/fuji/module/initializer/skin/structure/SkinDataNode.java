package io.github.sakurawald.fuji.module.initializer.skin.structure;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SkinDataNode {
    String playerName;
    PropertyWrapper skinProperty;
}
