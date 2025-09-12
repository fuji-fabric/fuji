package io.github.sakurawald.fuji.module.initializer.nametag;

import io.github.sakurawald.fuji.core.document.annotation.ColorBox;
import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.core.auxiliary.LogUtil;
import io.github.sakurawald.fuji.core.config.handler.abst.BaseConfigurationHandler;
import io.github.sakurawald.fuji.core.config.handler.impl.ObjectConfigurationHandler;
import io.github.sakurawald.fuji.core.document.annotation.TestCase;
import io.github.sakurawald.fuji.module.initializer.ModuleInitializer;
import io.github.sakurawald.fuji.module.initializer.nametag.config.model.NametagConfigModel;
import io.github.sakurawald.fuji.module.initializer.nametag.service.NametagService;

@Document(id = 1751825018627L, value = """
    Customize the nametag above the players.
    """)
@ColorBox(id = 1751978505336L, color = ColorBox.ColorBoxTypes.EXAMPLE, value = """
    ◉ Set the background of nametag to blue color.
    Set `background` to `-16776961` (The integer representation of blue color)

    ◉ Set the half transparency for nametag.
    Set `text_opacity` to `128`.

    ◉ Scale the size of text into double.
    Set the `x`, `y`, and `z` in `scale` to `2.0`.
    """)
@TestCase(action = "Pass through a nether portal.", targets = {
    "The nametag entity should be removed in the old dimension."
    , "A new nametag entity should be created in the new dimension."
    , "A new nametag entity should be created after the use of `nether portal`"
    , "A new nametag entity should be created after the use of `ender portal`"
    , "A new nametag entity should be created after the use of `/player Steve spawn`"
    , "A new nametag entity should be removed after the use of `/kill Steve`"
    , "A new nametag entity should be seen after mounting a `pig` entity."
    , "A new nametag entity should be seen after dis-mounting a `pig` entity."
})
public class NametagInitializer extends ModuleInitializer {

    public static final BaseConfigurationHandler<NametagConfigModel> config = ObjectConfigurationHandler.ofModule(BaseConfigurationHandler.CONFIG_JSON_LITERAL, NametagConfigModel.class);

    @Override
    protected void onReload() {
        LogUtil.debug("Remove all the created nametag entities. (Reason: module reloaded)");
        NametagService.removeAllNametagEntities();
    }

}
