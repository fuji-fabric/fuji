package mod.fuji.core.gui.structure;


import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;

public class SimpleGuiDuck extends SimpleGui implements SlotGuiInterfaceDuck
{
    public SimpleGuiDuck(MenuType<?> type, ServerPlayer player, boolean manipulatePlayerSlots) {
        super(type, player, manipulatePlayerSlots);
    }

    public void onVirtualGuiClose() {

    }

    #if MC_VER < MC_26_1
    @Override
    public final void onClose() {
        this.onVirtualGuiClose();
    }
    #elif MC_VER >= MC_26_1
    @Override
    public final void onManualClose() {
        this.onVirtualGuiClose();
    }

    @Override
    public void onPlayerClose(boolean success) {
        this.onVirtualGuiClose();
    }
   #endif

}
