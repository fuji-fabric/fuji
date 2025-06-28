package io.github.sakurawald.fuji.module.initializer.motd;

import com.google.common.base.Preconditions;
import io.github.sakurawald.fuji.core.annotation.Document;
import io.github.sakurawald.fuji.core.auxiliary.LogUtil;
import io.github.sakurawald.fuji.core.auxiliary.RandomUtil;
import io.github.sakurawald.fuji.core.auxiliary.ReflectionUtil;
import io.github.sakurawald.fuji.core.config.handler.abst.BaseConfigurationHandler;
import io.github.sakurawald.fuji.core.config.handler.impl.ObjectConfigurationHandler;
import io.github.sakurawald.fuji.module.initializer.ModuleInitializer;
import io.github.sakurawald.fuji.module.initializer.motd.config.model.MotdConfigModel;
import io.github.sakurawald.fuji.module.initializer.motd.structure.MotdEntry;
import lombok.Cleanup;
import net.minecraft.server.ServerMetadata;
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

@Document("""
    Customize the MOTD of the server.
    """)
public class MotdInitializer extends ModuleInitializer {

    private static final BaseConfigurationHandler<MotdConfigModel> config = new ObjectConfigurationHandler<>(BaseConfigurationHandler.CONFIG_JSON, MotdConfigModel.class);

    private static final Path ICON_FOLDER = ReflectionUtil.computeModuleConfigPath(MotdInitializer.class).resolve("icon");

    public static @NotNull Optional<ServerMetadata.Favicon> getMotdIcon(@Nullable String preferIcon) {
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
            LogUtil.error("Failed to read icon %s, did you forget to put the image file in proper location?".formatted(preferIcon), e);
            return Optional.empty();
        }

        return Optional.of(new ServerMetadata.Favicon(byteArrayOutputStream.toByteArray()));
    }

    public static @NotNull MotdEntry getMotdEntry() {
        return RandomUtil.drawList(config.model().entries);
    }

}
