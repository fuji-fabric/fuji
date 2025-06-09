package io.github.sakurawald.core.manager.impl.command;

import io.github.sakurawald.core.command.processor.CommandAnnotationProcessor;
import io.github.sakurawald.core.manager.abst.BaseManager;

public class CommandManager extends BaseManager {

    @Override
    public void onInitialize() {
        CommandAnnotationProcessor.process();
    }
}
