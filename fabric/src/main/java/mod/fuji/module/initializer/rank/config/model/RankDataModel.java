package mod.fuji.module.initializer.rank.config.model;

import mod.fuji.module.initializer.rank.structure.RankDataNode;
import java.util.HashMap;
import java.util.Map;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class RankDataModel {

    Map<String, RankDataNode> rankDataNodeMap = new HashMap<>();
}
