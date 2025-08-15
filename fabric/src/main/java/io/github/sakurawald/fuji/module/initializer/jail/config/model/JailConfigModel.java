package io.github.sakurawald.fuji.module.initializer.jail.config.model;

import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.core.structure.GlobalPos;
import io.github.sakurawald.fuji.module.initializer.jail.structure.JailDescriptor;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class JailConfigModel {

    String noJailStatusText = "<grey>[None]";

    String jailedPlayerTabListText = "<dark_red>[Prisoner] %player:name%</dark_red>";

    @Document(id = 1753684834618L, value = "A `jail descriptor` is used to define a `jail`.")
    List<JailDescriptor> jailDescriptors = new ArrayList<>() {
        {
            this.add(JailDescriptor.make("example", new GlobalPos("minecraft:overworld", 0, 64, 0, 0, 0)));
        }
    };

}
