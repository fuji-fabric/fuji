package io.github.sakurawald.core.gui.layer;

import eu.pb4.sgui.api.elements.GuiElementInterface;
import eu.pb4.sgui.api.gui.layered.Layer;

public class SingleLineLayer extends Layer {

    public SingleLineLayer() {
        super(1, 9);
    }

    public SingleLineLayer(GuiElementInterface guiElementInterface) {
        this();
        for (int i = 0; i < this.getWidth(); i++) {
            this.setSlot(i, guiElementInterface);
        }
    }

}
