package mod.fuji.module.initializer.pvp.config.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
public class PvPDataModel {

    public @NotNull Set<String> whitelist = new HashSet<>();

}
