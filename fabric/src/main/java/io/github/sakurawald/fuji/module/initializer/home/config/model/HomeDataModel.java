package io.github.sakurawald.fuji.module.initializer.home.config.model;

import com.google.gson.annotations.SerializedName;
import io.github.sakurawald.fuji.core.structure.GlobalPos;
import java.util.concurrent.ConcurrentHashMap;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class HomeDataModel {

    @SerializedName(value = "name2home", alternate = "homes")
    public @NotNull ConcurrentHashMap<String, Map<String, GlobalPos>> name2home = new ConcurrentHashMap<>();
}
