package mod.fuji.core.gui.structure;


public interface GuiClickCallbackDuck
    #if MC_VER < MC_26_1
    extends eu.pb4.sgui.api.elements.GuiElementInterface.ClickCallback
    #elif MC_VER >= MC_26_1
    extends eu.pb4.sgui.api.elements.GuiElement.ClickCallback
    #endif {

}
