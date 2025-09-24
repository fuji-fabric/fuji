package mod.fuji.core.document.gui;

import com.mojang.brigadier.context.CommandContext;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import eu.pb4.sgui.api.gui.SimpleGui;
import mod.fuji.core.auxiliary.minecraft.CommandHelper;
import mod.fuji.core.auxiliary.minecraft.TextHelper;
import mod.fuji.core.command.descriptor.CommandDescriptor;
import mod.fuji.core.command.structure.CommandRequirementDescriptor;
import mod.fuji.core.document.auxiliary.DocumentUtil;
import mod.fuji.core.gui.component.gui.PagedGui;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.minecraft.item.Items;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class CommandsInspectionGui extends PagedGui<CommandDescriptor> {

    public CommandsInspectionGui(@Nullable SimpleGui parent, @NotNull ServerPlayerEntity player, @NotNull List<CommandDescriptor> entities, int pageIndex) {
        super(parent, player, TextHelper.getTextByKey(player, "fuji.inspect.fuji_commands.gui.title"), entities, pageIndex);
    }

    public static CommandsInspectionGui inspectAll(SimpleGui parent, ServerPlayerEntity player) {
        List<CommandDescriptor> entities = DocumentUtil.getCommandDescriptors();

        return new CommandsInspectionGui(parent, player, entities, 0);
    }

    public static int inspectCommandDescriptors(CommandContext<ServerCommandSource> ctx, Predicate<CommandDescriptor> filter) {
        Stream<CommandDescriptor> commandDescriptorStream = CommandDescriptor.REGISTERED_COMMAND_DESCRIPTORS
            .stream()
            .filter(filter);

        ServerCommandSource source = ctx.getSource();
        return Optional.ofNullable(source.getPlayer())
            .map(player -> {
                new CommandsInspectionGui(null, player, commandDescriptorStream.toList(), 0).open();
                return CommandHelper.Return.SUCCESS;
            })
            .orElseGet(() -> {
                commandDescriptorStream.forEach(it -> TextHelper.sendMessageByText(source, Text.literal(it.getCommandNodePath())));
                return CommandHelper.Return.SUCCESS;
            });
    }

    @Override
    protected PagedGui<CommandDescriptor> makePage(@Nullable SimpleGui parent, @NotNull ServerPlayerEntity player, Text title, @NotNull List<CommandDescriptor> entities, int pageIndex) {
        return new CommandsInspectionGui(parent, player, entities, pageIndex);
    }

    private @NotNull List<Text> asLore(@NotNull CommandDescriptor entity) {
        List<Text> lore = new ArrayList<>();

        /* Attach method document. */
        if (entity.document.isPresent()) {
            List<Text> methodDocumentTextList = TextHelper.getDocumentTextList(getPlayer(), entity.document.get());
            lore.addAll(methodDocumentTextList);
        }

        /* Attach parameters document. */
        List<Text> parameterDocumentTextList = entity.commandArguments
            .stream()
            .filter(it -> it.getDocument() != null)
            .map(it -> {
                Text documentText = TextHelper.getDocumentText(getPlayer(), "◉ %s: %s".formatted(it.getArgumentName(), it.getDocument()));
                return documentText;
            }).toList();
        lore.addAll(parameterDocumentTextList);

        return lore;
    }

    @SuppressWarnings("CollectionAddAllCanBeReplacedWithConstructor")
    @Override
    protected @NotNull GuiElementInterface toGuiElement(@NotNull CommandDescriptor entity) {
        List<Text> lore = new ArrayList<>();

        /* Add basic properties of command descriptor. */
        CommandRequirementDescriptor commandRequirement = CommandDescriptor.CommandRequirement.computeCommandRequirement(entity);

        lore.addAll(List.of(
            TextHelper.getTextByKey(getPlayer(),"from_module", entity.getSourceModule())
            , TextHelper.getTextByKey(getPlayer(), "command.source.can_be_executed_by_console", entity.canBeExecutedByConsole())
            , TextHelper.getTextByKey(getPlayer(), "command.descriptor.type", entity.getClass().getSimpleName())
            , TextHelper.getTextByKey(getPlayer(), "command.requirement.level_permission", commandRequirement.getLevel())
            , TextHelper.getTextByKey(getPlayer(), "command.requirement.string_permission", commandRequirement.getString())
        ));

        /* Add documents lore of this command. */
        List<Text> documents = asLore(entity);
        if (!documents.isEmpty()) {
            documents.add(0, Text.empty());
            lore.addAll(documents);
        }

        /* Make the GUI. */
        return new GuiElementBuilder()
            .setName(Text.literal(entity.getUserFriendlyCommandSyntax()))
            .setItem(Items.REPEATING_COMMAND_BLOCK)
            .setLore(lore)
            .build();
    }

}
