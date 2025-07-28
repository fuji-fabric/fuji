package io.github.sakurawald.fuji.module.initializer.jail.structure;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class JailDataNode {

    List<PrisonerRecord> records = new ArrayList<>();

}
