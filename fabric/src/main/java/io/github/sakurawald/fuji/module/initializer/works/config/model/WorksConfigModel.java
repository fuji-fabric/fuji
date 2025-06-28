package io.github.sakurawald.fuji.module.initializer.works.config.model;

import io.github.sakurawald.fuji.core.document.annotation.Document;

public class WorksConfigModel {
    @Document("""
        The `duration` used to `sample` for `production work`.
        """)
    public int sample_time_ms = 60 * 1000 * 60;

    @Document("""
        The `max distance` allowed for `production work`.
        """)
    public int sample_distance_limit = 512;

    @Document("""
        The max types of items to display for `production work`.
        """)
    public int sample_counter_top_n = 20;
}

