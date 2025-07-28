package io.github.sakurawald.fuji.module.initializer.jail.service;

import io.github.sakurawald.fuji.module.initializer.jail.JailInitializer;
import io.github.sakurawald.fuji.module.initializer.jail.structure.JailDescriptor;
import java.util.List;
import java.util.Optional;

public class JailService {

    public static List<JailDescriptor> getJailDescriptors() {
        return JailInitializer.config.model().getJailDescriptors();
    }

    public static List<String> getJailIds() {
        return getJailDescriptors()
            .stream()
            .map(JailDescriptor::getId)
            .toList();
    }

    public static Optional<JailDescriptor> findJailDescriptor(String id) {
        return getJailDescriptors()
            .stream()
            .filter(it -> it.getId().equals(id))
            .findFirst();
    }


}
