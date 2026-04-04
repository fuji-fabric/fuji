package mod.fuji.core.gui.component.layer;

import eu.pb4.sgui.api.gui.layered.Layer;
import mod.fuji.core.auxiliary.minecraft.GuiHelper;
import mod.fuji.core.gui.structure.GuiElementIR;
import org.jetbrains.annotations.NotNull;

public class SingleLineLayer extends Layer {

    public SingleLineLayer() {
        super(1, 9);
    }

    public SingleLineLayer(@NotNull GuiElementIR guiElementInterface) {
        this();
        for (int i = 0; i < this.getWidth(); i++) {
            GuiHelper.setSlot(this, i, guiElementInterface);
        }
    }

}
