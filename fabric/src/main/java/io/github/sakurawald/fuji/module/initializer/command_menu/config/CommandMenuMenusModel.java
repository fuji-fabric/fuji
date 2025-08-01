package io.github.sakurawald.fuji.module.initializer.command_menu.config;

import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.RegistryHelper;
import io.github.sakurawald.fuji.module.initializer.command_menu.structure.MenuDescriptor;
import io.github.sakurawald.fuji.module.initializer.command_menu.structure.SlotDescriptor;
import lombok.Data;
import net.minecraft.item.Items;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class CommandMenuMenusModel {

    @Document(id = 1751824827629L, value = """
        Defined `menus`.
        """)
    public Map<String, MenuDescriptor> menus = new HashMap<>() {
        {
            /* Menu: example-menu */
            MenuDescriptor exampleMenu = new MenuDescriptor();
            exampleMenu.setTitle("<blue>My Example Menu");
            exampleMenu.setLines(6);
            exampleMenu.setCommands(new MenuDescriptor.Commands());
            exampleMenu.setCloseMenuOnClicked(false);
            exampleMenu.setSlots(new ArrayList<>() {
                {
                    /* slot 1 */
                    SlotDescriptor slot1 = new SlotDescriptor();
                    slot1.index = 0;
                    this.add(slot1);

                    /* slot 2 */
                    SlotDescriptor slot2 = new SlotDescriptor();
                    slot2.index = 1;
                    slot2.item = RegistryHelper.getIdAsString(Items.APPLE);
                    slot2.lore = new ArrayList<>();
                    slot2.glow = true;
                    slot2.displayName = "<green>Click to to open another menu.";
                    slot2.commands.on_left_click_commands = List.of("command-menu open %player:name% another-menu");
                    this.add(slot2);
                }
            });
            this.put("example-menu", exampleMenu);


            /* Menu: another-menu */
            MenuDescriptor secondMenu = new MenuDescriptor();
            secondMenu.setTitle("<blue>Another menu.");
            secondMenu.setLines(2);
            secondMenu.setCommands(new MenuDescriptor.Commands());
            secondMenu.setCloseMenuOnClicked(false);
            secondMenu.setSlots(new ArrayList<>() {
                {
                    /* slot 1 */
                    SlotDescriptor slot1 = new SlotDescriptor();
                    slot1.index = 0;
                    slot1.setCount(2);
                    slot1.setLore(new ArrayList<>());
                    slot1.setDisplayName("This is another menu.");
                    slot1.setItem(RegistryHelper.getIdAsString(Items.GOLDEN_APPLE));
                    this.add(slot1);

                    /* slot 2 */
                    SlotDescriptor slot2 = new SlotDescriptor();
                    slot2.index = 1;
                    slot2.setCount(1);
                    slot2.setLore(new ArrayList<>());
                    slot2.setDisplayName("Click me to refresh: %server:uptime%");
                    slot2.commands.on_left_click_commands = List.of("command-menu open %player:name% another-menu");
                    slot2.setItem(RegistryHelper.getIdAsString(Items.CLOCK));
                    this.add(slot2);
                }
            });
            this.put("another-menu", secondMenu);

        }
    };
}
