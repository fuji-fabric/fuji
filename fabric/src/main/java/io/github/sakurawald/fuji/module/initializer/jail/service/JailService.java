package io.github.sakurawald.fuji.module.initializer.jail.service;

import io.github.sakurawald.fuji.module.initializer.jail.JailInitializer;
import io.github.sakurawald.fuji.module.initializer.jail.structure.JailDescriptor;
import java.util.List;

public class JailService {

    public static List<JailDescriptor> getJailDescriptors() {
        return JailInitializer.config.model().getJailDescriptors();
    }



}
