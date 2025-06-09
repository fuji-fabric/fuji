package io.github.sakurawald.module.initializer.command_toolbox.warp.config.model;

import com.google.gson.annotations.SerializedName;
import io.github.sakurawald.module.initializer.command_toolbox.warp.structure.WarpNode;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class WarpDataModel {
    @SerializedName(value = "name2warp", alternate = "warps")
    public @NotNull Map<String, WarpNode> name2warp = new HashMap<>();
}
