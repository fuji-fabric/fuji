package io.github.sakurawald.fuji.core.gui.impl.layer;

import eu.pb4.sgui.api.elements.GuiElementInterface;
import eu.pb4.sgui.api.gui.layered.Layer;
import org.jetbrains.annotations.NotNull;

public class SingleLineLayer extends Layer {

    public SingleLineLayer() {
        super(1, 9);
    }

    public SingleLineLayer(@NotNull GuiElementInterface guiElementInterface) {
        this();
        for (int i = 0; i < this.getWidth(); i++) {
            this.setSlot(i, guiElementInterface);
        }
    }

}
