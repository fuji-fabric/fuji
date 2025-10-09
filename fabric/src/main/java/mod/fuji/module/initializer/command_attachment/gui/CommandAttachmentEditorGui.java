package mod.fuji.module.initializer.command_attachment.gui;

import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import eu.pb4.sgui.api.gui.SimpleGui;
import mod.fuji.core.auxiliary.CollectionUtil;
import mod.fuji.core.auxiliary.minecraft.EntityHelper;
import mod.fuji.core.auxiliary.minecraft.GuiHelper;
import mod.fuji.core.auxiliary.minecraft.TextHelper;
import mod.fuji.core.auxiliary.minecraft.UuidHelper;
import mod.fuji.core.auxiliary.minecraft.WorldHelper;
import mod.fuji.core.command.exception.AbortCommandExecutionException;
import mod.fuji.core.gui.component.gui.ConfirmSignGui;
import mod.fuji.core.gui.component.gui.PagedGui;
import mod.fuji.module.initializer.command_attachment.service.CommandAttachmentService;
import mod.fuji.module.initializer.command_attachment.structure.CommandAttachmentDataNode;
import mod.fuji.module.initializer.command_attachment.structure.attachment_entry.BaseCommandAttachmentEntry;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CommandAttachmentEditorGui extends PagedGui<BaseCommandAttachmentEntry> {

    private final CommandAttachmentDataNode commandAttachmentDataNode;

    public CommandAttachmentEditorGui(@Nullable SimpleGui parent, @NotNull ServerPlayerEntity player, @NotNull List<BaseCommandAttachmentEntry> entities, int pageIndex, CommandAttachmentDataNode commandAttachmentDataNode) {
        super(parent, player, TextHelper.getTextByKey(player, "command_attachment.editor.gui.title", commandAttachmentDataNode.getId()), entities, pageIndex);
        this.commandAttachmentDataNode = commandAttachmentDataNode;

        GuiHelper.Placer.setSlotInLastLine(this, 4, GuiHelper.Button
            .makeHelpButton(player)
            .setLore(TextHelper.getTextListByKey(player, "command_attachment.editor.help.lore")));
    }

    @Override
    protected @NotNull PagedGui<BaseCommandAttachmentEntry> makePage(@Nullable SimpleGui parent, @NotNull ServerPlayerEntity player, Text title, @NotNull List<BaseCommandAttachmentEntry> entities, int pageIndex) {
        return new CommandAttachmentEditorGui(parent, player, entities, pageIndex, this.commandAttachmentDataNode);
    }

    @SuppressWarnings("OptionalIsPresent")
    private static Optional<String> getTargetUuidForEditor(@NotNull ServerPlayerEntity player) {
        /* Try to get UUID from looking at entity. */
        Optional<String> lookingAtEntityUUID = WorldHelper.Raycast
            .getLookingAtEntity(player)
            .map(UuidHelper::getAttachedUuid)
            .filter(uuid -> CommandAttachmentService.findAttachmentDataNode(uuid).isPresent());
        if (lookingAtEntityUUID.isPresent()) {
            return lookingAtEntityUUID;
        }

        /* Try to get UUID from looking at block. */
        ServerWorld world = EntityHelper.getServerWorld(player);
        Optional<String> lookingAtBlockUUID = WorldHelper.Raycast
            .getLookingAtBlock(player)
            .map(block -> UuidHelper.getAttachedUuid(world, block))
            .filter(uuid -> CommandAttachmentService.findAttachmentDataNode(uuid).isPresent());
        if (lookingAtBlockUUID.isPresent()) {
            return lookingAtBlockUUID;
        }

        /* Try to get UUID from main hand item. */
        ItemStack mainHandStack = player.getMainHandStack();
        Optional<String> mainHandItemUUID = UuidHelper.getAttachedUuid(mainHandStack);
        if (mainHandItemUUID.isPresent()) {
            return mainHandItemUUID;
        }

        /* Failed to get the UUID. */
        return Optional.empty();
    }

    public static @NotNull CommandAttachmentEditorGui make(@NotNull ServerPlayerEntity player, @NotNull CommandAttachmentDataNode dataNode) {
        List<BaseCommandAttachmentEntry> entities = dataNode.getAttachments().getEntries();
        return new CommandAttachmentEditorGui(null, player, entities, 0, dataNode);
    }

    public static @NotNull CommandAttachmentEditorGui make(@NotNull ServerPlayerEntity player) {
        return getTargetUuidForEditor(player)
            .flatMap(CommandAttachmentService::findAttachmentDataNode)
            .map(dataNode -> make(player, dataNode))
            .orElseThrow(() -> {
                TextHelper.sendTextByKey(player, "command_attachment.editor.no_target");
                return new AbortCommandExecutionException();
            });
    }

    @Override
    protected @NotNull GuiElementInterface toGuiElement(@NotNull BaseCommandAttachmentEntry entity) {
        GuiElementBuilder builder = new GuiElementBuilder();

        builder.setItem(Items.NAME_TAG);
        builder.setName(TextHelper.getTextByKey(player, "command_attachment.attachment"));
        builder.setLore(entity.asLore(player));
        builder.setCallback((clickType) -> onEntityCallback(clickType, entity));

        return builder.build();
    }

    private void reopenThis() {
        CommandAttachmentEditorGui
            .make(player, commandAttachmentDataNode)
            .open();
    }

    @SuppressWarnings("UnnecessaryReturnStatement")
    private void onEntityCallback(@NotNull ClickType clickType, @NotNull BaseCommandAttachmentEntry entity) {
        CopyOnWriteArrayList<BaseCommandAttachmentEntry> entries = commandAttachmentDataNode.getAttachments().getEntries();

        /* Shift + Right = Delete */
        if (clickType.isRight && clickType.shift) {
            new ConfirmSignGui(this.getBackendGui(), this.getPlayer()) {
                @Override
                public void onConfirm() {
                    entries.remove(entity);
                    reopenThis();
                }

            }.open();
            return;
        }

        /* Left = Move entity left */
        if (clickType.isLeft) {
            CollectionUtil.moveElementLeft(entries, entity);
            reopenThis();
            return;
        }

        /* Right = Move entity right */
        if (clickType.isRight) {
            CollectionUtil.moveElementRight(entries, entity);
            reopenThis();
            return;
        }


    }
}
