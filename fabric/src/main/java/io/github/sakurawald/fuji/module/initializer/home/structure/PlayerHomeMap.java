package io.github.sakurawald.fuji.module.initializer.home.structure;

import io.github.sakurawald.fuji.core.structure.GlobalPos;
import java.util.HashMap;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class PlayerHomeMap extends HashMap<String, GlobalPos> {

}
