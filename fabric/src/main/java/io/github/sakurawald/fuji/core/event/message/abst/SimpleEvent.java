package io.github.sakurawald.fuji.core.event.message.abst;


import io.github.sakurawald.fuji.core.auxiliary.LogUtil;

import io.github.sakurawald.fuji.core.document.annotation.ForDeveloper;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

@ForDeveloper("""
    The Event<T> describe a `generic event type`.
    The `<T>` is used to describe a `specific event type`.
    An `event type` is defined by its method signature.
    The handlers holds the list of closure, to capture the needed surrounding variables.
    When an `specific event` is `fired`, we will iterate the handlers list, invoke the closures with method arguments.
    The invoker factory says: give me the list of handlers, and give me the specific event type, I will make the closure invoker for you.
    Each time you register a new event callback (or we said the closure), you need to make a new invoker.

    The invoker factory is a nested closure.
    The first level captures the handlers variable instance from the specific event instance.
    The second level captures the handlers argument from the first level, and iterates over it.

    In most case, the `mixin` is just better than `event`:
    1. Mixin gives you fine-grained control on time sequence.
    2. Mixin gives you the proper way to handle `priority` and `cancelled event`. (Better injection point)
    3. Mixin has better performance.
    4. Mixin gives you the tool to make events.

    For some convenient and stable event, you can use mixin to create abstraction for them.
    For example, the `player joined event`...
    """)
public class SimpleEvent<T> {

    private final List<T> handlers = new ArrayList<>();
    private final Function<List<T>, T> invokerFactory;
    private T invoker;

    public SimpleEvent(Function<List<T>, T> invokerFactory) {
        this.invokerFactory = invokerFactory;
        this.makeNewInvoker();
    }

    private void makeNewInvoker() {
        this.invoker = invokerFactory.apply(handlers);
    }

    public void register(T eventCallback) {
        LogUtil.debug("Register event callback: event = {}, callback = {}", this.invoker.getClass().getName(), eventCallback.getClass().getName());
        this.handlers.add(eventCallback);
        this.makeNewInvoker();
    }

    public T invoker() {
        return this.invoker;
    }

}
