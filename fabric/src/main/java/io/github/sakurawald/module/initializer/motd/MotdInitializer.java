package io.github.sakurawald.module.initializer.motd;

import com.google.common.base.Preconditions;
import io.github.sakurawald.core.auxiliary.LogUtil;
import io.github.sakurawald.core.auxiliary.RandomUtil;
import io.github.sakurawald.core.auxiliary.ReflectionUtil;
import io.github.sakurawald.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.core.config.handler.abst.BaseConfigurationHandler;
import io.github.sakurawald.core.config.handler.impl.ObjectConfigurationHandler;
import io.github.sakurawald.module.initializer.ModuleInitializer;
import io.github.sakurawald.module.initializer.motd.config.model.MotdConfigModel;
import lombok.Cleanup;
import net.minecraft.server.ServerMetadata;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Stream;

public class MotdInitializer extends ModuleInitializer {

    private static final BaseConfigurationHandler<MotdConfigModel> config = new ObjectConfigurationHandler<>(BaseConfigurationHandler.CONFIG_JSON, MotdConfigModel.class);

    private static final Path ICON_FOLDER = ReflectionUtil.computeModuleConfigPath(MotdInitializer.class).resolve("icon");

    public static @NotNull Optional<ServerMetadata.Favicon> getRandomIcon() {
        ByteArrayOutputStream byteArrayOutputStream;
        try {
            /* get icon files */
            Files.createDirectories(ICON_FOLDER);
            @Cleanup Stream<Path> list = Files.list(ICON_FOLDER);
            List<File> icons = list
                .map(Path::toFile)
                .toList();

            /* draw a random icon */
            if (icons.isEmpty()) {
                return Optional.empty();
            }

            File randomIcon = RandomUtil.drawList(icons);
            BufferedImage bufferedImage = ImageIO.read(randomIcon);

            Preconditions.checkState(bufferedImage.getWidth() == 64, "Must be 64 pixels wide: %s".formatted(randomIcon));
            Preconditions.checkState(bufferedImage.getHeight() == 64, "Must be 64 pixels high: %s".formatted(randomIcon));

            byteArrayOutputStream = new ByteArrayOutputStream();
            ImageIO.write(bufferedImage, "PNG", byteArrayOutputStream);
        } catch (Exception e) {
            LogUtil.error("failed to encode motd icon.", e);
            return Optional.empty();
        }

        return Optional.of(new ServerMetadata.Favicon(byteArrayOutputStream.toByteArray()));
    }

    public static @NotNull Text getRandomMotdText() {
        var motdList = config.model().list;
        String string = motdList.get(new Random().nextInt(motdList.size()));

        return TextHelper.getTextByValue(null, string);
    }

}
