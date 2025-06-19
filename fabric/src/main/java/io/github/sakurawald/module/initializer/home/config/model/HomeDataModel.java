package io.github.sakurawald.module.initializer.home.config.model;

import com.google.gson.annotations.SerializedName;
import io.github.sakurawald.core.structure.GlobalPos;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class HomeDataModel {

    @SerializedName(value = "name2home", alternate = "homes")
    public @NotNull Map<String, Map<String, GlobalPos>> name2home = new HashMap<>();
}
