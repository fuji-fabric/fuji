package mod.fuji.module.initializer.works.config.model;

import mod.fuji.core.document.annotation.Document;

public class WorksConfigModel {
    @Document(id = 1751825512657L, value = """
        The `duration` used for `sample` for `production work`.
        """)
    public int sample_time_ms = 60 * 1000 * 60;

    @Document(id = 1751825519653L, value = """
        The `max distance` allowed for `production work`.
        """)
    public int sample_distance_limit = 512;

    @Document(id = 1751825523340L, value = """
        The max types of items to display for `production work`.
        """)
    public int sample_counter_top_n = 20;
}

