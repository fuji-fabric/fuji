package io.github.sakurawald.module.initializer.back.config.model;

import io.github.sakurawald.core.annotation.Document;

public class BackConfigModel {
    @Document("""
        Ignore `this teleport` if the `distance` is too close in between.
        """)
    public double ignore_distance = 32d;

    @Document("""
        Should we save the location on player death?
        """)
    public boolean enable_back_on_death = true;

    @Document("""
        Should we save the location on player teleport?
        """)
    public boolean enable_back_on_teleport = true;

    @Document("""
        Max saved location slots.
        """)
    public int max_back_location_entries_to_save = 3;
}
