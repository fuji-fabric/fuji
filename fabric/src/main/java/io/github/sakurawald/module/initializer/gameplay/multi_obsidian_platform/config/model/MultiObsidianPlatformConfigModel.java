package io.github.sakurawald.module.initializer.gameplay.multi_obsidian_platform.config.model;

import io.github.sakurawald.core.annotation.Document;

public class MultiObsidianPlatformConfigModel {
    @Document("""
        The `coordinate scale factor` between `overworld` and `the_end`.
        """)
    public double factor = 4;
}
