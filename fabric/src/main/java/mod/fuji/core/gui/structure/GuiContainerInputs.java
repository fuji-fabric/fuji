package mod.fuji.core.gui.structure;

public class GuiContainerInputs {

    #if MC_VER < MC_26_1
    public static final net.minecraft.world.inventory.ClickType PICKUP = net.minecraft.world.inventory.ClickType.PICKUP;
    public static final net.minecraft.world.inventory.ClickType QUICK_MOVE = net.minecraft.world.inventory.ClickType.QUICK_MOVE;
    public static final net.minecraft.world.inventory.ClickType SWAP = net.minecraft.world.inventory.ClickType.SWAP;
    public static final net.minecraft.world.inventory.ClickType CLONE = net.minecraft.world.inventory.ClickType.CLONE;
    public static final net.minecraft.world.inventory.ClickType THROW = net.minecraft.world.inventory.ClickType.THROW;
    public static final net.minecraft.world.inventory.ClickType QUICK_CRAFT = net.minecraft.world.inventory.ClickType.QUICK_CRAFT;
    public static final net.minecraft.world.inventory.ClickType PICKUP_ALL = net.minecraft.world.inventory.ClickType.PICKUP_ALL;
    #elif MC_VER >= MC_26_1
    public static final net.minecraft.world.inventory.ContainerInput PICKUP = net.minecraft.world.inventory.ContainerInput.PICKUP;
    public static final net.minecraft.world.inventory.ContainerInput QUICK_MOVE = net.minecraft.world.inventory.ContainerInput.QUICK_MOVE;
    public static final net.minecraft.world.inventory.ContainerInput SWAP = net.minecraft.world.inventory.ContainerInput.SWAP;
    public static final net.minecraft.world.inventory.ContainerInput CLONE = net.minecraft.world.inventory.ContainerInput.CLONE;
    public static final net.minecraft.world.inventory.ContainerInput THROW = net.minecraft.world.inventory.ContainerInput.THROW;
    public static final net.minecraft.world.inventory.ContainerInput QUICK_CRAFT = net.minecraft.world.inventory.ContainerInput.QUICK_CRAFT;
    public static final net.minecraft.world.inventory.ContainerInput PICKUP_ALL = net.minecraft.world.inventory.ContainerInput.PICKUP_ALL;
    #endif

}
