package io.github.sakurawald.fuji.module.initializer.rank.structure;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RankRequirement {
    String description;
    List<String> commands;
}
