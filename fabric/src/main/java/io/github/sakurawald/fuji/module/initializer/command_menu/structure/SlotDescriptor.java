package io.github.sakurawald.fuji.module.initializer.command_menu.structure;

import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import eu.pb4.sgui.api.gui.SlotGuiInterface;
import io.github.sakurawald.fuji.core.document.annotation.DocStringProvider;
import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.GuiHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.LuckpermsHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.RegistryHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.fuji.core.command.executor.CommandExecutor;
import io.github.sakurawald.fuji.core.command.structure.ExtendedCommandSource;
import io.github.sakurawald.fuji.module.initializer.command_menu.CommandMenuInitializer;
import io.github.sakurawald.fuji.core.document.descriptor.PermissionDescriptor;
import lombok.Data;
import lombok.NoArgsConstructor;
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

    // NOTE: It's possible only provide the NBT field, but it's hard to use.

    @Document(id = 1751824853240L, value = """
        Where to place this item in GUI?
        """)
    public int index = 0;

    @Document(id = 1751824861377L, value = """
        What is the item?
        """)
    public String item = "minecraft:stone";

    @Document(id = 1751824865422L, value = """
        The count of this item.
        """)
    public int count = 42;

    @Document(id = 1751824870793L, value = """
        The display name of this item.
        """)
    public @Nullable String displayName = "<blue>My Nice Item Name";

    public boolean hideTooltip = false;

    @Document(id = 1751824877459L, value = """
        Should we glow this item?
        """)
    public boolean glow = false;

    @Document(id = 1751824881740L, value = """
        The lore of this item.
        """)
    public List<String> lore = new ArrayList<>() {
        {
            this.add("<green>Hello %player:name%");
            this.add("<yellow>You are in %world:id%");
        }
    };

    @Document(id = 1751824886812L, value = """
        The `requirement` to `see` this item in GUI.
        """)
    public ViewRequirement viewRequirement = new ViewRequirement();
    public static class ViewRequirement {
        // NOTE: The view requirement decides whether the player can see this slot in menu.
        public int level = 0;
        public @Nullable String string = null;
    }

    public Commands commands = new Commands();
    public static class Commands {
        public List<String> on_left_click_commands = new ArrayList<>(){
            {
                this.add("send-message %player:name% You just clicked me.");
                this.add("chain has-level? %player:name% 4 chain send-message %player:name% <yellow>You are op player.");
                this.add("command-menu close %player:name%");
            }
        };

        public List<String> on_left_shift_click_commands = new ArrayList<>();
        public List<String> on_right_click_commands = new ArrayList<>();
        public List<String> on_right_shift_click_commands = new ArrayList<>();
        public List<String> on_middle_click_commands = new ArrayList<>();
    }

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
            if (clickType == ClickType.MOUSE_LEFT && !slotDescriptor.commands.on_left_click_commands.isEmpty()) {
                CommandExecutor.execute(ExtendedCommandSource.asConsole(viewingPlayer.getCommandSource()), slotDescriptor.commands.on_left_click_commands);
                tryCloseThisMenu();
                return;
            }
            if (clickType == ClickType.MOUSE_RIGHT && !slotDescriptor.commands.on_right_click_commands.isEmpty()) {
                CommandExecutor.execute(ExtendedCommandSource.asConsole(viewingPlayer.getCommandSource()), slotDescriptor.commands.on_right_click_commands);
                tryCloseThisMenu();
                return;
            }
            if (clickType == ClickType.MOUSE_LEFT_SHIFT && !slotDescriptor.commands.on_left_shift_click_commands.isEmpty()) {
                CommandExecutor.execute(ExtendedCommandSource.asConsole(viewingPlayer.getCommandSource()), slotDescriptor.commands.on_left_shift_click_commands);
                tryCloseThisMenu();
                return;
            }
            if (clickType == ClickType.MOUSE_RIGHT_SHIFT && !slotDescriptor.commands.on_right_shift_click_commands.isEmpty()) {
                CommandExecutor.execute(ExtendedCommandSource.asConsole(viewingPlayer.getCommandSource()), slotDescriptor.commands.on_right_shift_click_commands);
                tryCloseThisMenu();
                return;
            }
            if (clickType == ClickType.MOUSE_MIDDLE && !slotDescriptor.commands.on_middle_click_commands.isEmpty()) {
                CommandExecutor.execute(ExtendedCommandSource.asConsole(viewingPlayer.getCommandSource()), slotDescriptor.commands.on_middle_click_commands);
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
        GuiElementBuilder slotElementBuilder = new GuiElementBuilder();

        slotElementBuilder.setItem(RegistryHelper.getItem(this.item));
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
