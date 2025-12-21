package mod.fuji.module.initializer.motd;

import com.google.common.base.Preconditions;
import mod.fuji.core.auxiliary.minecraft.TextHelper;
import mod.fuji.core.config.mapper.structure.GameProfileIR;
import mod.fuji.core.document.annotation.ColorBox;
import mod.fuji.core.document.annotation.Document;
import mod.fuji.core.auxiliary.LogUtil;
import mod.fuji.core.auxiliary.RandomUtil;
import mod.fuji.core.auxiliary.ReflectionUtil;
import mod.fuji.core.config.handler.abst.BaseConfigurationHandler;
import mod.fuji.core.config.handler.impl.ObjectConfigurationHandler;
import mod.fuji.core.event.annotation.EventConsumer;
import mod.fuji.core.event.message.server.metadata.ModifyServerMetadataEvent;
import mod.fuji.module.initializer.ModuleInitializer;
import mod.fuji.module.initializer.motd.config.model.MotdConfigModel;
import mod.fuji.module.initializer.motd.structure.MotdEntry;
import java.util.UUID;
import lombok.Cleanup;
import net.minecraft.network.protocol.status.ServerStatus;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Document(id = 1751826866342L, value = """
    Customize the MOTD of the server.
    """)
@ColorBox(id = 1751978213888L, color = ColorBox.ColorBoxTypes.EXAMPLE, value = """
    ◉ Configure server icons.
    You can put `64x64 pixels .png image file` into the directory `config/fuji/modules/motd/icon` dir.
    The module will pick up a random image as the icon of the server.
    To respond the server status request.
    """)
@ColorBox(id = 1751978273696L, color = ColorBox.ColorBoxTypes.TIP, value = """
    ◉ Use an `editor` to help you design the MOTD.
    Online MOTD editor: https://colorize.fun/en/minecraft
    """)
@ColorBox(id = 1753458678347L, color = ColorBox.ColorBoxTypes.TIP, value = """
    ◉ Customize the MOTD using `Formating Codes`.
    The `Formating Codes` is a vanilla Minecraft feature.
    See: https://minecraft.fandom.com/wiki/Formatting_codes
    """)
public class MotdInitializer extends ModuleInitializer {

    private static final BaseConfigurationHandler<MotdConfigModel> config = ObjectConfigurationHandler
        .ofModule(BaseConfigurationHandler.CONFIG_JSON_LITERAL, MotdConfigModel.class);

    private static final Path ICON_FOLDER = ReflectionUtil.computeModuleConfigPath(MotdInitializer.class).resolve("icon");

    public static Optional<ServerStatus.Favicon> getEffectiveMotdIcon(@Nullable String preferIcon) {
        ByteArrayOutputStream byteArrayOutputStream;
        try {
            /* Mkdir the icon dir. */
            Files.createDirectories(ICON_FOLDER);
            @Cleanup Stream<Path> temp = Files.list(ICON_FOLDER);
            List<File> availableIcons = temp
                .map(Path::toFile)
                .toList();

            /* Choose one icon. */
            File chooseItem;
            if (preferIcon == null) {
                if (availableIcons.isEmpty()) {
                    return Optional.empty();
                } else {
                    chooseItem = RandomUtil.drawList(availableIcons);
                }
            } else {
                chooseItem = ICON_FOLDER.resolve(preferIcon).toFile();
            }

            /* Read the icon file. */
            BufferedImage bufferedImage = ImageIO.read(chooseItem);
            Preconditions.checkState(bufferedImage.getWidth() == 64, "Must be 64 pixels wide: %s".formatted(chooseItem));
            Preconditions.checkState(bufferedImage.getHeight() == 64, "Must be 64 pixels high: %s".formatted(chooseItem));

            /* Create the image buffer. */
            byteArrayOutputStream = new ByteArrayOutputStream();
            ImageIO.write(bufferedImage, "PNG", byteArrayOutputStream);
        } catch (Exception e) {
            LogUtil.error("Failed to read icon file '{}', can't find it in '{}' directory.", preferIcon, ICON_FOLDER, e);
            return Optional.empty();
        }

        return Optional.of(new ServerStatus.Favicon(byteArrayOutputStream.toByteArray()));
    }

    public static @NotNull MotdEntry getEffectiveMotdEntry() {
        return RandomUtil.drawList(config.model().getMessages());
    }

    public static @NotNull ServerStatus.Players getEffectivePlayersInfo(@NotNull ServerStatus.Players original) {
        var configSection = config.model().getPlayersInfo();

        int deltaMax = RandomUtil.getRandomNumber(configSection.getMaxPlayers().getDeltaMin(), configSection.getMaxPlayers().getDeltaMax());
        int max = original.max() + deltaMax;

        int deltaOnline = RandomUtil.getRandomNumber(configSection.getOnlinePlayers().getDeltaMin(), configSection.getOnlinePlayers().getDeltaMax());
        int online = original.online() + deltaOnline;

        List<GameProfileIR> sample;
        if (configSection.getHoverText().isEnable()) {
            sample = configSection
                    .getHoverText()
                    .getLines()
                    .stream()
                    .map(line -> TextHelper.Parsers.parsePlaceholderString(null, line))
                    .map(line -> GameProfileIR.from(UUID.randomUUID(), line))
                    .toList();
        } else {
            sample = original.sample()
                .stream()
                .map(GameProfileIR::from)
                .toList();
        }

        var $sample = sample.stream()
            .map(GameProfileIR::toUserProfile)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .toList();
        return new ServerStatus.Players(max, online, $sample);
    }

    public static @NotNull ServerStatus.Version getEffectiveVersion(ServerStatus.Version original) {
        var configSection = config.model().getVersionText();

        if (configSection.isEnable()) {
            String text = configSection.getText();
            String gameVersion = TextHelper.Parsers.parsePlaceholderString(null, text);
            int protocolVersion = -42;
            return new ServerStatus.Version(gameVersion, protocolVersion);
        } else {
            return original;
        }

    }

    @EventConsumer
    private static void onRequestServerMetadataHandler(ModifyServerMetadataEvent event) {
        ServerStatus original = event.getServerMetadata();

        MotdEntry motdEntry = MotdInitializer.getEffectiveMotdEntry();
        Component text = TextHelper.getTextByValue(null, motdEntry.getText());
        Optional<ServerStatus.Favicon> icon = MotdInitializer.getEffectiveMotdIcon(motdEntry.getIcon());
        Optional<ServerStatus.Players> players = original.players().map(MotdInitializer::getEffectivePlayersInfo);
        Optional<ServerStatus.Version> version = original.version().map(MotdInitializer::getEffectiveVersion);

        ServerStatus serverMetadata = new ServerStatus(text, players, version, icon, original.enforcesSecureChat());
        event.setServerMetadata(serverMetadata);
    }

}
