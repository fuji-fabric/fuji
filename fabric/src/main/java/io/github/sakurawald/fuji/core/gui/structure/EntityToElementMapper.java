package io.github.sakurawald.fuji.core.gui.structure;

import eu.pb4.sgui.api.elements.GuiElementInterface;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class EntityToElementMapper<T>  {

    final Map<T, GuiElementInterface> bindings = new HashMap<>();

    public void clearBindings() {
        this.bindings.clear();
    }

    public void setBinding(T entity, GuiElementInterface element) {
        this.bindings.put(entity, element);
    }

    public @Nullable GuiElementInterface getBinding(T entity) {
        return this.bindings.get(entity);
    }

}
