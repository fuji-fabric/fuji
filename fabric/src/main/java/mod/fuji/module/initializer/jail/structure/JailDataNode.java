package mod.fuji.module.initializer.jail.structure;

import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JailDataNode {

    String jailId;
    List<JailRecord> records = new ArrayList<>();

    public static JailDataNode makeDefault(JailDescriptor descriptor) {
        JailDataNode jailDataNode = new JailDataNode();
        jailDataNode.setJailId(descriptor.getId());
        return jailDataNode;
    }
}
