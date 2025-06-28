package io.github.sakurawald.fuji.core.manager.impl.command;

import io.github.sakurawald.fuji.core.command.processor.CommandAnnotationProcessor;
import io.github.sakurawald.fuji.core.manager.abst.BaseManager;

public class CommandManager extends BaseManager {

    @Override
    public void onInitialize() {
        CommandAnnotationProcessor.process();
    }
}
