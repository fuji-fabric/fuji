package mod.fuji.module.initializer.skin.structure;

import mod.fuji.core.config.mapper.wrapper.PropertyIR;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SkinDataNode {
    String playerName;
    PropertyIR skinProperty;
}
