package mod.fuji.core.gui.structure;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class EntityToElementMapping<T>  {

    final Map<T, GuiElementIR> bindings = new HashMap<>();

    public void clearBindings() {
        this.bindings.clear();
    }

    public void setBinding(@NotNull T entity, @NotNull GuiElementIR element) {
        this.bindings.put(entity, element);
    }

    public @Nullable GuiElementIR getBinding(@NotNull T entity) {
        return this.bindings.get(entity);
    }

}
