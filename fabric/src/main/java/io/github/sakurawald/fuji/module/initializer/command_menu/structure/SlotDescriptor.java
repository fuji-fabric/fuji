package io.github.sakurawald.fuji.module.initializer.command_menu.structure;

import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import eu.pb4.sgui.api.gui.SlotGuiInterface;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.ItemStackHelper;
import io.github.sakurawald.fuji.core.document.annotation.DocStringProvider;
import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.GuiHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.LuckpermsHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.fuji.core.command.executor.CommandExecutor;
import io.github.sakurawald.fuji.core.command.executor.structure.ExtendedCommandSource;
import io.github.sakurawald.fuji.module.initializer.command_menu.CommandMenuInitializer;
import io.github.sakurawald.fuji.core.document.descriptor.PermissionDescriptor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class SlotDescriptor {

    @DocStringProvider(id = 1751999474601L, value = """
        To view this slot, you need the defined `specified permission` for this slot.
        """)
    private static final PermissionDescriptor SLOT_VIEW_REQUIREMENT_PERMISSION = new PermissionDescriptor("<specified-permission>", 1751999474601L);

    @Document(id = 1751824853240L, value = """
        Where to place this item in GUI?
        """)
    int index = 0;

    List<Integer> otherIndexes = new ArrayList<>();

    @Document(id = 1751824861377L, value = """
        What is the item?
        """)
    String item = "minecraft:stone";

    @Document(id = 1751824865422L, value = """
        The count of this item.
        """)
    int count = 42;

    @Document(id = 1751824870793L, value = """
        The display name of this item.
        """)
    @Nullable String displayName = "<blue>My Nice Item Name";

    boolean hideTooltip = false;

    @Document(id = 1751824877459L, value = """
        Should we glow this item?
        """)
    boolean glow = false;

    @Document(id = 1751824881740L, value = """
        The lore of this item.
        """)
    List<String> lore = new ArrayList<>() {
        {
            this.add("<green>Hello %player:name%");
            this.add("<yellow>You are in %world:id%");
        }
    };

    @Document(id = 1751824886812L, value = """
        The `requirement` to `see` this item in GUI.
        """)

    ViewRequirement viewRequirement = new ViewRequirement();

    @Data
    @NoArgsConstructor
    public static class ViewRequirement {
        // NOTE: The view requirement decides whether the player can see this slot in menu.
        int level = 0;
        @Nullable String string = null;
    }

    Commands commands = new Commands();

    @Data
    @NoArgsConstructor
    public static class Commands {
        List<String> onLeftClickCommands = new ArrayList<>(){
            {
                this.add("send-message %player:name% You just clicked me.");
                this.add("chain has-level? %player:name% 4 chain send-message %player:name% <yellow>You are op player.");
                this.add("command-menu close %player:name%");
            }
        };

        List<String> onLeftShiftClickCommands = new ArrayList<>();
        List<String> onRightClickCommands = new ArrayList<>();
        List<String> onRightShiftClickCommands = new ArrayList<>();
        List<String> onMiddleClickCommands = new ArrayList<>();
    }

    @SuppressWarnings("RedundantIfStatement")
    public boolean canViewThisSlot(ServerPlayerEntity player) {
        if (!player.hasPermissionLevel(this.viewRequirement.level)) return false;
        if (this.viewRequirement.string != null
            && !this.viewRequirement.string.isEmpty()
            && !LuckpermsHelper.hasPermission(player.getUuid(), SLOT_VIEW_REQUIREMENT_PERMISSION, this.viewRequirement.string)) return false;

        return true;
    }

    public record CommandMenuSlotClickCallback(ServerPlayerEntity viewingPlayer, MenuDescriptor menuDescriptor, SlotDescriptor slotDescriptor) implements GuiElementInterface.ClickCallback {

        @SuppressWarnings("UnnecessaryReturnStatement")
        @Override
        public void click(int i, ClickType clickType, SlotActionType clickType1, @NotNull SlotGuiInterface slotGuiInterface) {

            /* Dispatch click type. */
            if (clickType == ClickType.MOUSE_LEFT && !slotDescriptor.commands.onLeftClickCommands.isEmpty()) {
                CommandExecutor.executeBatch(ExtendedCommandSource.asConsole(viewingPlayer.getCommandSource()), slotDescriptor.commands.onLeftClickCommands);
                tryCloseThisMenu();
                return;
            }
            if (clickType == ClickType.MOUSE_RIGHT && !slotDescriptor.commands.onRightClickCommands.isEmpty()) {
                CommandExecutor.executeBatch(ExtendedCommandSource.asConsole(viewingPlayer.getCommandSource()), slotDescriptor.commands.onRightClickCommands);
                tryCloseThisMenu();
                return;
            }
            if (clickType == ClickType.MOUSE_LEFT_SHIFT && !slotDescriptor.commands.onLeftShiftClickCommands.isEmpty()) {
                CommandExecutor.executeBatch(ExtendedCommandSource.asConsole(viewingPlayer.getCommandSource()), slotDescriptor.commands.onLeftShiftClickCommands);
                tryCloseThisMenu();
                return;
            }
            if (clickType == ClickType.MOUSE_RIGHT_SHIFT && !slotDescriptor.commands.onRightShiftClickCommands.isEmpty()) {
                CommandExecutor.executeBatch(ExtendedCommandSource.asConsole(viewingPlayer.getCommandSource()), slotDescriptor.commands.onRightShiftClickCommands);
                tryCloseThisMenu();
                return;
            }
            if (clickType == ClickType.MOUSE_MIDDLE && !slotDescriptor.commands.onMiddleClickCommands.isEmpty()) {
                CommandExecutor.executeBatch(ExtendedCommandSource.asConsole(viewingPlayer.getCommandSource()), slotDescriptor.commands.onMiddleClickCommands);
                tryCloseThisMenu();
                return;
            }

        }

        private void tryCloseThisMenu() {
            /* Close the menu if the slot is clicked. */
            if (menuDescriptor.closeMenuOnClicked) {
                CommandMenuInitializer.closeCurrentHandledScreen(viewingPlayer);
            }
        }
    }

    public GuiElementInterface buildGuiElement(ServerPlayerEntity viewingPlayer, MenuDescriptor menuDescriptor) {
        ItemStack itemStack = ItemStackHelper.Parser.parseItemStack(this.item);
        GuiElementBuilder slotElementBuilder = GuiElementBuilder.from(itemStack);

        slotElementBuilder.setCount(this.count);

        if (this.hideTooltip) {
            GuiHelper.hideTooltip(slotElementBuilder);
        }

        if (this.glow) {
            slotElementBuilder.glow();
        }

        if (this.displayName != null) {
            Text displayName = TextHelper.getTextByValue(viewingPlayer, this.displayName);
            slotElementBuilder.setName(displayName);
        }

        if (this.lore != null && !this.lore.isEmpty()) {
            List<Text> lore = new ArrayList<>();
            this.lore.forEach(it -> lore.add(TextHelper.getTextByValue(viewingPlayer, it)));
            slotElementBuilder.setLore(lore);
        }

        slotElementBuilder.setCallback(new CommandMenuSlotClickCallback(viewingPlayer, menuDescriptor, this));

        return slotElementBuilder.build();
    }
}
