package io.github.sakurawald.fuji.module.initializer.warning.config.model;

import io.github.sakurawald.fuji.module.initializer.warning.structure.PlayerWarnings;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class WarningDataModel {

    public List<PlayerWarnings> players = new ArrayList<>();

}
