package mod.fuji.module.initializer.jail.config.model;

import mod.fuji.module.initializer.jail.structure.JailDataNode;
import java.util.concurrent.CopyOnWriteArrayList;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class JailDataModel {

    CopyOnWriteArrayList<JailDataNode> jailDataNodes = new CopyOnWriteArrayList<>();

}
