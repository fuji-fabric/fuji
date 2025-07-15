package io.github.sakurawald.fuji.module.initializer.world.border.config.model;

import io.github.sakurawald.fuji.module.initializer.world.border.structure.BorderDescriptor;
import java.util.ArrayList;
import java.util.List;

public class WorldBorderConfigModel {

    public List<BorderDescriptor> borders = new ArrayList<>() {
        {
            this.add(new BorderDescriptor("fuji:example"));
        }
    };
}
