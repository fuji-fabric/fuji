package io.github.sakurawald.fuji.module.initializer.jail.config.model;

import io.github.sakurawald.fuji.module.initializer.jail.structure.JailDataNode;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class JailDataModel {

    List<JailDataNode> jailDataNodes = new ArrayList<>();

}
