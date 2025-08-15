package io.github.sakurawald.fuji.module.initializer.title.config.model;

import io.github.sakurawald.fuji.module.initializer.title.structure.TitlePreference;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class TitleDataModel {
    List<TitlePreference> preferences = new ArrayList<>();
}
