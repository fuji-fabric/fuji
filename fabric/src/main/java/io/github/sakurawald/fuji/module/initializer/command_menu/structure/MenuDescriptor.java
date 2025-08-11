package io.github.sakurawald.fuji.module.initializer.command_menu.structure;

import eu.pb4.sgui.api.gui.SimpleGui;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.GuiHelper;
import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.fuji.core.command.executor.CommandExecutor;
import io.github.sakurawald.fuji.core.command.executor.structure.ExtendedCommandSource;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;

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

    private ScreenHandlerType<?> getScreenHandlerType() {
        return GuiHelper.Handler.getGenericContainerType(this.lines);
    }

    public SimpleGui build(ServerPlayerEntity viewingPlayer) {
        /* Make the menu GUI. */
        SimpleGui menuGui = new SimpleGui(this.getScreenHandlerType(), viewingPlayer, false) {
            @Override
            public void onOpen() {
                super.onOpen();
                CommandExecutor.execute(ExtendedCommandSource.asConsole(viewingPlayer.getCommandSource()), commands.on_open_commands);
            }

            @Override
            public void onClose() {
                super.onClose();
                CommandExecutor.execute(ExtendedCommandSource.asConsole(viewingPlayer.getCommandSource()), commands.on_closed_commands);
            }
        };
        menuGui.setTitle(TextHelper.getTextByValue(viewingPlayer, this.title));

        /* Place defined slots in the menu GUI. */
        this.slots.forEach(slotDescriptor -> {
            if (slotDescriptor.canViewThisSlot(viewingPlayer)) {
                menuGui.setSlot(slotDescriptor.index, slotDescriptor.buildGuiElement(viewingPlayer, this));
            }
        });
        return menuGui;
    }

}
