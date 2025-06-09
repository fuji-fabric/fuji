package io.github.sakurawald.core.event.abst;


import io.github.sakurawald.core.auxiliary.LogUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class Event<T> {

    private final Function<List<T>, T> invokerFactory;
    private final List<T> handlers = new ArrayList<>();
    private T invoker;

    public Event(Function<List<T>, T> invokerFactory) {
        this.invokerFactory = invokerFactory;
        this.updateInvoker();
    }

    private void updateInvoker() {
        this.invoker = invokerFactory.apply(handlers);
    }

    public void register(T eventCallback) {
        LogUtil.debug("register event callback: {}", eventCallback.getClass().getName());
        this.handlers.add(eventCallback);
        this.updateInvoker();
    }

    public T invoker() {
        return this.invoker;
    }

}
