package io.github.sakurawald.fuji.module.initializer.gameplay.multi_obsidian_platform.config.model;

import io.github.sakurawald.fuji.core.document.annotation.Document;

public class MultiObsidianPlatformConfigModel {
    @Document("""
        The `coordinate scale factor` between `overworld` and `the_end`.
        """)
    public double factor = 4;
}
