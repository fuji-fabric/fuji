package mod.fuji.module.initializer.command_toolbox.warp.config.model;

import com.google.gson.annotations.SerializedName;
import mod.fuji.module.initializer.command_toolbox.warp.structure.WarpDescriptor;
import java.util.concurrent.ConcurrentHashMap;
import org.jetbrains.annotations.NotNull;

public class WarpDataModel {

    @SerializedName(value = "warps", alternate = "name2warp")
    public @NotNull ConcurrentHashMap<String, WarpDescriptor> warps = new ConcurrentHashMap<>();
}
