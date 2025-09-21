package mod.fuji.module.initializer.world.border.config.model;

import mod.fuji.core.document.annotation.Document;
import mod.fuji.module.initializer.world.border.structure.BorderDescriptor;
import java.util.ArrayList;
import java.util.List;

public class WorldBorderConfigModel {

    @Document(id = 1752567783661L, value = """
        Define the `border` for each `dimension`.
        """)
    public List<BorderDescriptor> borders = new ArrayList<>() {
        {
            this.add(new BorderDescriptor("fuji:example"));
        }
    };
}
