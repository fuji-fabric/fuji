package mod.fuji.module.initializer.skin.config.model;


import mod.fuji.module.initializer.skin.structure.SkinDataNode;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class SkinDataModel {
    List<SkinDataNode> nodes = new ArrayList<>();
}
