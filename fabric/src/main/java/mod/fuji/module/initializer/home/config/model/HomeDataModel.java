package mod.fuji.module.initializer.home.config.model;

import com.google.common.collect.HashBiMap;
import com.google.gson.annotations.SerializedName;
import mod.fuji.core.config.mapper.structure.PlayerKey;
import mod.fuji.core.structure.GlobalPos;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

@Data
@NoArgsConstructor
public class HomeDataModel {

    @SerializedName(value = "name2home", alternate = "homes")
    @NotNull ConcurrentHashMap<PlayerKey, HashBiMap<String, GlobalPos>> name2home = new ConcurrentHashMap<>();

}
