package mod.fuji.module.initializer.back.config.model;

import mod.fuji.core.document.annotation.Document;

public class BackConfigModel {
    @Document(id = 1751825546269L, value = """
        Ignore `this teleport` if the `distance` is too close in between.
        """)
    public double ignore_distance = 32d;

    @Document(id = 1751825551657L, value = """
        Should we save the location on player death?
        """)
    public boolean enable_back_on_death = true;

    @Document(id = 1751825557852L, value = """
        Should we save the location on player teleport?
        """)
    public boolean enable_back_on_teleport = true;

    @Document(id = 1751825562969L, value = """
        Max saved location slots.
        """)
    public int max_back_location_entries_to_save = 3;
}
