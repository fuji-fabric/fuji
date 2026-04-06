package mod.fuji.core.gui.structure;

public interface SlotGuiInterfaceDuck
    #if MC_VER < MC_26_1
    extends eu.pb4.sgui.api.gui.SlotGuiInterface
    #elif MC_VER >= MC_26_1
    extends eu.pb4.sgui.api.gui.SlotBasedGui
    #endif

{

}
