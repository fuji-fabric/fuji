package mod.fuji.module.initializer.sign_editor;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import mod.fuji.core.auxiliary.CollectionUtil;
import mod.fuji.core.auxiliary.minecraft.CommandHelper;
import mod.fuji.core.auxiliary.minecraft.TextHelper;
import mod.fuji.core.command.annotation.CommandNode;
import mod.fuji.core.command.annotation.CommandRequirement;
import mod.fuji.core.command.annotation.CommandSource;
import mod.fuji.core.command.argument.wrapper.impl.GreedyString;
import mod.fuji.core.document.annotation.Document;
import mod.fuji.module.initializer.ModuleInitializer;
import mod.fuji.module.initializer.sign_editor.command.argument.wrapper.SignLine;
import mod.fuji.module.initializer.sign_editor.service.SignEditorService;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.DyeColor;


@Document(id = 1778874535621L, value = """
    This module provides a sign editor to edit the texts on a sign block.
    """)
@CommandNode("sign-edit")
@CommandRequirement(level = 4)
public class SignEditorInitializer extends ModuleInitializer {

    @CommandNode("lock")
    @Document(id = 1779044844670L, value = "Set the `lock` state of the sign block.")
    private static int $lock(@CommandSource ServerPlayer player, Optional<Boolean> lock) {
        return SignEditorService.selectLookingAtSignBlock(player, blockPos -> {
            return SignEditorService.withSignBlockEntity(player, blockPos, signBlockEntity -> {
                boolean newValue = lock.orElseGet(() -> !signBlockEntity.isWaxed());
                signBlockEntity.setWaxed(newValue);
                return CommandHelper.Return.SUCCESS;
            });
        });
    }

    @CommandNode("glow")
    @Document(id = 1779044865958L, value = "Set the `glow` state of the sign block.")
    private static int $glow(@CommandSource ServerPlayer player, Optional<Boolean> glow, Optional<Boolean> frontSide, Optional<Boolean> bothSides) {
        return SignEditorService
            .selectLookingAtSignBlock(player, blockPos -> {
                return SignEditorService.withSignBlockEntity(player, blockPos, signBlockEntity -> {
                    SignEditorService.updateSignText(player, signBlockEntity, frontSide, bothSides, signText -> {
                        boolean newValue = glow.orElseGet(() -> !signText.hasGlowingText());
                        return signText.setHasGlowingText(newValue);
                    });
                    return CommandHelper.Return.SUCCESS;
                });
            });

    }

    @CommandNode("set-color")
    @Document(id = 1779044873906L, value = "Set the `color` of the sign block.")
    private static int $setColor(@CommandSource ServerPlayer player, DyeColor color, Optional<Boolean> frontSide, Optional<Boolean> bothSides) {
        return SignEditorService.selectLookingAtSignBlock(player, blockPos -> {
            return SignEditorService.withSignBlockEntity(player, blockPos, signBlockEntity -> {
                SignEditorService.updateSignText(player, signBlockEntity, frontSide, bothSides, signText -> {
                    return signText.setColor(color);
                });
                return CommandHelper.Return.SUCCESS;
            });
        });
    }

    @CommandNode("set-line")
    @Document(id = 1779044903372L, value = "Set the text of the specified line of the sign block.")
    private static int $setLine(@CommandSource ServerPlayer player, SignLine line, Optional<Boolean> frontSide, Optional<Boolean> bothSides, GreedyString text) {
        return SignEditorService.selectLookingAtSignBlock(player, blockPos -> {
            return SignEditorService.withSignBlockEntity(player, blockPos, signBlockEntity -> {
                SignEditorService.updateSignText(player, signBlockEntity, frontSide, bothSides, signText -> {
                    final int index = line.getValue() - 1;
                    Component component = TextHelper.getTextByValue(player, text.getValue());
                    return signText.setMessage(index, component);
                });
                return CommandHelper.Return.SUCCESS;
            });
        });
    }

    @CommandNode("set-all")
    @Document(id = 1779044929459L, value = "Set the texts of all lines of the sign block.")
    private static int $setAll(@CommandSource ServerPlayer player, Optional<Boolean> frontSide, Optional<Boolean> bothSides, GreedyString text) {
        /* Split and pad the lines. */
        List<String> lines = Arrays
            .stream(text.getValue().split("\\\\n"))
            .collect(Collectors.toList());
        CollectionUtil.padRight(lines, SignEditorService.MAX_SIGN_BLOCK_LINES, () -> "");

        /* Dispatch the operation to the primitive function. */
        for (int currentLine = 1; currentLine <= lines.size(); currentLine++) {
            final SignLine signLine = new SignLine(currentLine);
            final GreedyString lineText = new GreedyString(lines.get(currentLine - 1));
            $setLine(player, signLine, frontSide, bothSides, lineText);
        }
        return CommandHelper.Return.SUCCESS;
    }

    @CommandNode("mirror")
    @Document(id = 1779044962269L, value = "Mirror the looking side to another side.")
    private static int $mirror(@CommandSource ServerPlayer player) {
        return SignEditorService.selectLookingAtSignBlock(player, blockPos -> {
            return SignEditorService.withSignBlockEntity(player, blockPos, signBlockEntity -> {
                boolean facingFrontText = signBlockEntity.isFacingFrontText(player);
                boolean anotherSide = !facingFrontText;
                SignEditorService.updateSignText(player, signBlockEntity, Optional.of(anotherSide), Optional.of(false), signText -> {
                    return signBlockEntity.getText(facingFrontText);
                });
                return CommandHelper.Return.SUCCESS;
            });
        });
    }

}
