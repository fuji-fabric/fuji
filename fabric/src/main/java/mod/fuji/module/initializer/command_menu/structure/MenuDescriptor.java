package mod.fuji.module.initializer.command_menu.structure;

import eu.pb4.sgui.api.elements.GuiElementInterface;
import eu.pb4.sgui.api.gui.SimpleGui;
import mod.fuji.core.auxiliary.minecraft.GuiHelper;
import mod.fuji.core.document.annotation.Document;
import mod.fuji.core.auxiliary.minecraft.TextHelper;
import mod.fuji.core.command.executor.CommandExecutor;
import mod.fuji.core.command.executor.structure.ExtendedCommandSource;
import java.util.concurrent.atomic.AtomicReference;
import lombok.Data;
import lombok.NoArgsConstructor;
import mod.fuji.core.gui.structure.GuiElementIR;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class MenuDescriptor {

    @Document(id = 1751824832095L, value = """
        The `title` of this GUI.
        """)
    public String title;

    @Document(id = 1751824836843L, value = """
        Ranged [1, 6]
        """)
    public int lines;


    @Document(id = 1751824840934L, value = """
        Should we `close` this GUI automatically when any `slot` is `clicked`?

        Or you need to execute `/command-menu close <player>` command.
        To close the GUI manually.
        """)
    public boolean closeMenuOnClicked;

    public Commands commands;
    public static class Commands {
        public List<String> on_open_commands = new ArrayList<>();
        public List<String> on_closed_commands = new ArrayList<>();
    }

    @Document(id = 1751824845545L, value = """
        Defined `slots` for this GUI.
        """)
    public List<SlotDescriptor> slots;

    private MenuType<?> getScreenHandlerType() {
        return GuiHelper.Handler.getGenericContainerType(this.lines);
    }

    public SimpleGui build(ServerPlayer viewingPlayer) {
        /* Make the menu GUI. */
        SimpleGui menuGui = new SimpleGui(this.getScreenHandlerType(), viewingPlayer, false) {
            @Override
            public void onOpen() {
                super.onOpen();
                CommandExecutor.executeBatch(ExtendedCommandSource.asConsole(viewingPlayer.createCommandSourceStack()), commands.on_open_commands);
            }

            @Override
            public void onClose() {
                super.onClose();
                CommandExecutor.executeBatch(ExtendedCommandSource.asConsole(viewingPlayer.createCommandSourceStack()), commands.on_closed_commands);
            }
        };
        menuGui.setTitle(TextHelper.getTextByValue(viewingPlayer, this.title));

        /* Place defined slots in the menu GUI. */
        AtomicReference<SlotDescriptor> blankSlotsFiller = new AtomicReference<>();

        this.slots.forEach(slotDescriptor -> {
            if (slotDescriptor.canViewThisSlot(viewingPlayer)) {
                /* Make the GUI element. */
                GuiElementInterface element = slotDescriptor.buildGuiElement(viewingPlayer, this);

                /* Set primary index. */
                menuGui.setSlot(slotDescriptor.getIndex(), element);

                /* Set other indexes. */
                slotDescriptor.getOtherIndexes().forEach(otherIndex -> {
                    menuGui.setSlot(otherIndex, element);
                });

                /* Set blank slots filler. */
                if (slotDescriptor.isFillBlankIndexes()) {
                    blankSlotsFiller.set(slotDescriptor);
                }
            }
        });

        /* Process the blank slots filler. */
        if (blankSlotsFiller.get() != null) {
            GuiElementIR element = GuiElementIR
                .of(blankSlotsFiller
                    .get()
                    .buildGuiElement(viewingPlayer, this));
            GuiHelper.Placer.fillEmptySlots(menuGui, element);
        }

        return menuGui;
    }

}
