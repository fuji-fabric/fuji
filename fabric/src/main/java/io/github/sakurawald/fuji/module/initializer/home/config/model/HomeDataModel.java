package io.github.sakurawald.fuji.module.initializer.home.config.model;

import com.google.gson.annotations.SerializedName;
import io.github.sakurawald.fuji.module.initializer.home.structure.PlayerHomeMap;
import java.util.concurrent.ConcurrentHashMap;
import org.jetbrains.annotations.NotNull;

public class HomeDataModel {

    @SerializedName(value = "name2home", alternate = "homes")
    public @NotNull ConcurrentHashMap<String, PlayerHomeMap> name2home = new ConcurrentHashMap<>();
}
