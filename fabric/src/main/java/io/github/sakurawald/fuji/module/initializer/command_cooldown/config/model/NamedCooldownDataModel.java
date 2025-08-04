package io.github.sakurawald.fuji.module.initializer.command_cooldown.config.model;

import io.github.sakurawald.fuji.module.initializer.command_cooldown.structure.NamedCooldownDataNode;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class NamedCooldownDataModel {
    List<NamedCooldownDataNode> nodes = new ArrayList<>();
}
