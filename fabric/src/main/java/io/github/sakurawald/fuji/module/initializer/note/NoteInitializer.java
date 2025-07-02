package io.github.sakurawald.fuji.module.initializer.note;


import io.github.sakurawald.fuji.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.ServerHelper;
import io.github.sakurawald.fuji.core.command.annotation.CommandNode;
import io.github.sakurawald.fuji.core.command.annotation.CommandRequirement;
import io.github.sakurawald.fuji.core.command.annotation.CommandSource;
import io.github.sakurawald.fuji.core.config.handler.abst.BaseConfigurationHandler;
import io.github.sakurawald.fuji.core.config.handler.impl.ObjectConfigurationHandler;
import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.core.document.descriptor.PermissionDescriptor;
import io.github.sakurawald.fuji.module.initializer.ModuleInitializer;
import io.github.sakurawald.fuji.module.initializer.note.config.model.NoteDataModel;
import io.github.sakurawald.fuji.module.initializer.note.gui.WarningGui;
import java.util.List;
import net.minecraft.server.network.ServerPlayerEntity;

public class NoteInitializer extends ModuleInitializer {

    public static PermissionDescriptor VIEW_WARNINGS_PERMISSION = new PermissionDescriptor("fuji.note.view", """
        To `view` the `warnings` of a `player`.
        """);

    public static PermissionDescriptor CREATE_WARNINGS_PERMISSION = new PermissionDescriptor("fuji.note.view", """
        To `create` a new `note` for a `player`.
        """);

    public static PermissionDescriptor DELETE_WARNINGS_PERMISSION = new PermissionDescriptor("fuji.note.view", """
        To `delete` a new `note` for a `player`.
        """);


    private static final BaseConfigurationHandler<NoteDataModel> data = new ObjectConfigurationHandler<>("note-data.json", NoteDataModel.class);

    @Document("Open the note GUI.")
    @CommandNode("note")
    @CommandRequirement(level = 4)
    private static int $warning(@CommandSource ServerPlayerEntity player) {
        List<String> offlinePlayerNames = ServerHelper.getOfflinePlayerNames();
        new WarningGui(null, player, offlinePlayerNames, 0)
            .open();
        return CommandHelper.Return.SUCCESS;
    }

}
