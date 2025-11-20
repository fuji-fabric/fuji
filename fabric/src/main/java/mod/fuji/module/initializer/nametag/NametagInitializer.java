package mod.fuji.module.initializer.nametag;

import mod.fuji.core.auxiliary.minecraft.CommandHelper;
import mod.fuji.core.command.annotation.CommandNode;
import mod.fuji.core.command.annotation.CommandRequirement;
import mod.fuji.core.command.annotation.CommandSource;
import mod.fuji.core.command.annotation.CommandTarget;
import mod.fuji.core.document.annotation.ColorBox;
import mod.fuji.core.document.annotation.Document;
import mod.fuji.core.auxiliary.LogUtil;
import mod.fuji.core.config.handler.abst.BaseConfigurationHandler;
import mod.fuji.core.config.handler.impl.ObjectConfigurationHandler;
import mod.fuji.core.document.annotation.TestCase;
import mod.fuji.module.initializer.ModuleInitializer;
import mod.fuji.module.initializer.nametag.config.model.NametagConfigModel;
import mod.fuji.module.initializer.nametag.config.model.NametagDataModel;
import mod.fuji.module.initializer.nametag.service.NametagService;
import mod.fuji.module.initializer.nametag.structure.NametagPlayerPreferences;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;

@Document(id = 1751825018627L, value = """
    Customize the nametag above the players.
    """)
@ColorBox(id = 1757696964602L, color = ColorBox.ColorBoxTypes.NOTE, value = """
    ◉ How it works?
    1. A `nametag entity` is a `text display entity`.
    2. A `nametag entity` is a `virtual entity`.
    2.a. The `entity` is not actually presented in the `server-side world`
    2.b. The `server` simulates the existence of `the nametag entity` on the `client-side world`, allowing the client to perceive it as if it were a real entity.

    Read the semantics of each field:
    - https://minecraft.wiki/w/Display
    """)
@ColorBox(id = 1751978505336L, color = ColorBox.ColorBoxTypes.EXAMPLE, value = """
    ◉ Set the background of nametag to blue color.
    Set `background` to `-16776961` (The integer representation of blue color)

    ◉ Set the half transparency for nametag.
    Set `text_opacity` to `128`.

    ◉ Scale the size of text into double.
    Set the `x`, `y`, and `z` in `scale` to `2.0`.
    """)
@ColorBox(id = 1757698545173L, color = ColorBox.ColorBoxTypes.EXAMPLE, value = """
    ◉ Toggle the `nametag entity` for self.
    Issue: `/nametag toggle @s`

    ◉ Turn on the `nametag entity` for online players.
    Issue: `/nametag toggle others @a true`

    ◉ Turn off the `nametag entity` for online players.
    Issue: `/nametag toggle others @a false`
    """)
@TestCase(action = "Test the functionality of `nametag` module.", targets = {
    "The nametag entity should be removed in the old dimension."
    , "A new nametag entity should be created in the new dimension."
    , "A new nametag entity should be created after the use of `nether portal`"
    , "A new nametag entity should be created after the use of `ender portal`"
    , "A new nametag entity should be created after the use of `/player Steve spawn`"
    , "The old nametag entity should be removed after the use of `/kill Steve`"
    , "A new nametag entity should be seen after mounting a `pig` entity."
    , "A new nametag entity should be seen after dis-mounting a `pig` entity."
})
public class NametagInitializer extends ModuleInitializer {

    public static final BaseConfigurationHandler<NametagConfigModel> config = ObjectConfigurationHandler
        .ofModule(BaseConfigurationHandler.CONFIG_JSON_LITERAL, NametagConfigModel.class);

    public static final BaseConfigurationHandler<NametagDataModel> data = ObjectConfigurationHandler
        .ofModule("nametag-data.json", NametagDataModel.class);

    @Override
    protected void onReload() {
        LogUtil.debug("Remove all the created nametag entities. (Reason: module reloaded)");
        NametagService.removeAllNametagEntities();
    }

    @CommandNode("nametag toggle")
    @CommandRequirement(level = 4)
    private static int $toggle(@CommandSource CommandSourceStack source, @CommandTarget ServerPlayer target) {
        NametagPlayerPreferences preferences = NametagService.getOrCreateNametagPlayerPreferences(target);
        boolean flag = !preferences.isEnableNametagEntity();
        return $toggle(source, target, flag);
    }

    @CommandNode("nametag toggle")
    @CommandRequirement(level = 4)
    private static int $toggle(@CommandSource CommandSourceStack source, @CommandTarget ServerPlayer target, boolean flag) {
        NametagPlayerPreferences preferences = NametagService.getOrCreateNametagPlayerPreferences(target);
        preferences.setEnableNametagEntity(flag);
        data.writeStorage();
        return CommandHelper.Return.SUCCESS;
    }

}
