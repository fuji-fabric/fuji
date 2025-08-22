package io.github.sakurawald.fuji.module.initializer.command_cooldown.config.model;

import io.github.sakurawald.fuji.module.initializer.command_cooldown.structure.NamedCooldownDataNode;
import java.util.concurrent.CopyOnWriteArrayList;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class NamedCooldownDataModel {
    CopyOnWriteArrayList<NamedCooldownDataNode> nodes = new CopyOnWriteArrayList<>();
}
