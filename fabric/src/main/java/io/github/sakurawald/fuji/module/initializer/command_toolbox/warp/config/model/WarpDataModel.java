package io.github.sakurawald.fuji.module.initializer.command_toolbox.warp.config.model;

import com.google.gson.annotations.SerializedName;
import io.github.sakurawald.fuji.module.initializer.command_toolbox.warp.structure.WarpNode;
import java.util.concurrent.ConcurrentHashMap;
import org.jetbrains.annotations.NotNull;

public class WarpDataModel {
    @SerializedName(value = "name2warp", alternate = "warps")
    public @NotNull ConcurrentHashMap<String, WarpNode> name2warp = new ConcurrentHashMap<>();
}
