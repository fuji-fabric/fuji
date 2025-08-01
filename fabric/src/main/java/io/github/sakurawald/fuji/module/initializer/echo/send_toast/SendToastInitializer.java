package io.github.sakurawald.fuji.module.initializer.echo.send_toast;

import io.github.sakurawald.fuji.core.auxiliary.LogUtil;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.RegistryHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.fuji.core.command.annotation.CommandNode;
import io.github.sakurawald.fuji.core.command.annotation.CommandRequirement;
import io.github.sakurawald.fuji.core.command.annotation.CommandSource;
import io.github.sakurawald.fuji.core.command.argument.wrapper.impl.GreedyString;
import io.github.sakurawald.fuji.core.command.exception.AbortCommandExecutionException;
import io.github.sakurawald.fuji.core.document.annotation.ColorBox;
import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.module.initializer.ModuleInitializer;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementCriterion;
import net.minecraft.advancement.AdvancementDisplay;
import net.minecraft.advancement.AdvancementFrame;
import net.minecraft.advancement.AdvancementProgress;

#if MC_VER <= MC_1_20_1
import java.util.HashMap;
#elif MC_VER > MC_1_20_1
import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.advancement.AdvancementRequirements;
#endif

import net.minecraft.advancement.AdvancementRewards;
import net.minecraft.advancement.criterion.CriterionProgress;
import net.minecraft.advancement.criterion.ImpossibleCriterion;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.network.packet.s2c.play.AdvancementUpdateS2CPacket;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

#if MC_VER >= MC_1_21_5
import net.minecraft.util.AssetInfo;
#endif

import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Document(id = 1751976160832L, value = """
    This module provides `/send-toast` command.
    To send the `text` as `toast` to a specified player.
    """)
@ColorBox(id = 1751976364671L, color = ColorBox.ColorBlockTypes.EXAMPLE, value = """
    ◉ Send a toast to a player.
    Issue: `/send-toast Alice --icon minecraft:golden_carrot \\<rb\\>eat this carrot`
    """)
public class SendToastInitializer extends ModuleInitializer {

    private static final String IMPOSSIBLE = "impossible";
    private static final String DUMMY_RESOURCE_IMAGE_IDENTIFIER = "minecraft:textures/gui/advancements/backgrounds/end.png";
    private static final Identifier SEND_TOAST_IDENTIFIER = Identifier.of("custom", "custom");

    private static void sendToast(ServerPlayerEntity player, AdvancementFrame advancementFrame, Item icon, Text title) {
        /* Make an advancement display. */
        AdvancementDisplay advancementDisplay = new AdvancementDisplay(
            icon.getDefaultStack()
            , title
            , Text.empty()
            ,
                #if MC_VER <= MC_1_20_2
                    RegistryHelper.makeIdentifierOrThrow(DUMMY_RESOURCE_IMAGE_IDENTIFIER)
                #elif MC_VER > MC_1_20_2 && MC_VER <= MC_1_21_4
                    Optional.of(RegistryHelper.makeIdentifier(DUMMY_RESOURCE_IMAGE_IDENTIFIER))
                #elif MC_VER >= MC_1_21_5
                    Optional.of(new AssetInfo(RegistryHelper.makeIdentifierOrThrow(DUMMY_RESOURCE_IMAGE_IDENTIFIER)))
                #endif

            , advancementFrame // Type of display frame.
            , true // Show toast.
            , false // Don't announce the progress to chat.
            , true // Hide this advancement display.
        );

        /* Make advancement entry. */
        #if MC_VER <= MC_1_20_1
        Advancement advancementEntry
        #elif MC_VER > MC_1_20_1
        AdvancementEntry advancementEntry
        #endif
            = Advancement.Builder
            .create()
            .display(advancementDisplay)
            .rewards(AdvancementRewards.NONE)
            .requirements(makeAdvancementRequirements())
            .criterion(IMPOSSIBLE, makeAdvancementCriterion())
            .build(SEND_TOAST_IDENTIFIER);

        /* Send packets. */
        player.networkHandler.sendPacket(makeGrantPacket(advancementEntry, SEND_TOAST_IDENTIFIER));
        player.networkHandler.sendPacket(makeRevokePacket(SEND_TOAST_IDENTIFIER));
    }

    @SuppressWarnings("UnnecessaryLocalVariable")
    private static
    #if MC_VER <= MC_1_20_1
    AdvancementCriterion
    #elif MC_VER > MC_1_20_1
    AdvancementCriterion<ImpossibleCriterion.Conditions>
    #endif
    makeAdvancementCriterion() {
        #if MC_VER <= MC_1_20_1
            AdvancementCriterion advancementCriterion = new AdvancementCriterion(new ImpossibleCriterion.Conditions());
        #elif MC_VER > MC_1_20_1
            AdvancementCriterion<ImpossibleCriterion.Conditions> advancementCriterion = new ImpossibleCriterion().create(new ImpossibleCriterion.Conditions());
        #endif

        return advancementCriterion;
    }

    private static
    #if MC_VER <= MC_1_20_1
    String[][]
    #elif MC_VER > MC_1_20_1
    AdvancementRequirements
    #endif
    makeAdvancementRequirements() {
        List<String> collection = List.of(IMPOSSIBLE);

        #if MC_VER <= MC_1_20_1
            return new String[][]{collection.toArray(String[]::new)};
        #elif MC_VER > MC_1_20_1 && MC_VER <= MC_1_20_2
            String[][] impossible = {{IMPOSSIBLE}};
            return new AdvancementRequirements(impossible);
        #elif MC_VER > MC_1_20_2
            return new AdvancementRequirements(List.of(List.of(IMPOSSIBLE)));
        #endif
    }

    private static AdvancementProgress makeAdvancementProgress() {
        AdvancementProgress advancementProgress = new AdvancementProgress();

        #if MC_VER <= MC_1_20_1
        Map<String, AdvancementCriterion> maps = new HashMap<>();
        maps.put(IMPOSSIBLE, new AdvancementCriterion(new ImpossibleCriterion.Conditions()));
        advancementProgress.init(maps, makeAdvancementRequirements());
        #elif MC_VER > MC_1_20_1
        advancementProgress.init(makeAdvancementRequirements());
        #endif

        return advancementProgress;
    }

    @SuppressWarnings("SameParameterValue")
    private static @NotNull AdvancementUpdateS2CPacket makeGrantPacket(
        #if MC_VER <= MC_1_20_1
        Advancement advancementEntry
        #elif MC_VER > MC_1_20_1
        AdvancementEntry advancementEntry
        #endif
        , Identifier identifier) {

        /* Make advancement progress. */
        AdvancementProgress advancementProgress = makeAdvancementProgress();

        /* Call obtain for criterion progress. */
        CriterionProgress criterionProgress = advancementProgress.getCriterionProgress(IMPOSSIBLE);
        if (criterionProgress == null) {
            LogUtil.error("It's strange that the statement `advancementProgress.getCriterionProgress(IMPOSSIBLE) is null, abort this advancement packet making.`");
            throw new AbortCommandExecutionException();
        }
        criterionProgress.obtain();

        /* Send the packet. */
        #if MC_VER <= MC_1_20_1
        Collection<Advancement> toEarn = List.of(advancementEntry);
        #elif MC_VER > MC_1_20_1
        Collection<AdvancementEntry> toEarn = List.of(advancementEntry);
        #endif

        Set<Identifier> toRemove = Set.of();
        Map<Identifier, AdvancementProgress> toSetProgress = Map.of(identifier, advancementProgress);
        return makeAdvancementUpdatePacket(toEarn, toRemove, toSetProgress);
    }

    private static AdvancementUpdateS2CPacket makeAdvancementUpdatePacket(
        #if MC_VER <= MC_1_20_1
        Collection<Advancement> toEarn
        #elif MC_VER > MC_1_20_1
        Collection<AdvancementEntry> toEarn
        #endif
        , Set<Identifier> toRemove, Map<Identifier, AdvancementProgress> toSetProgress)
    {
        #if MC_VER <= MC_1_21_4
            return new AdvancementUpdateS2CPacket(false, toEarn, toRemove, toSetProgress);
        #elif MC_VER >= MC_1_21_5
            return new AdvancementUpdateS2CPacket(false, toEarn, toRemove, toSetProgress, true);
        #endif
    }

    @SuppressWarnings("SameParameterValue")
    private static @NotNull AdvancementUpdateS2CPacket makeRevokePacket(Identifier identifier) {
        #if MC_VER <= MC_1_20_1
            Collection<Advancement> toEarn = List.of();
        #elif MC_VER > MC_1_20_1
            Collection<AdvancementEntry> toEarn = List.of();
        #endif

        Set<Identifier> toRemove = Set.of(identifier);
        Map<Identifier, AdvancementProgress> toSetProgress = Map.of();
        return makeAdvancementUpdatePacket(toEarn, toRemove, toSetProgress);
    }

    @CommandNode("send-toast")
    @CommandRequirement(level = 4)
    private static int $sendToast(@CommandSource ServerCommandSource source
        , ServerPlayerEntity player
        , Optional<AdvancementFrame> toastType
        , Optional<Item> icon
        , GreedyString message
    ) {
        Item $icon = icon.orElse(Items.SLIME_BALL);
        AdvancementFrame $toastType = toastType.orElse(AdvancementFrame.CHALLENGE);
        Text title = TextHelper.getTextByValue(player, message.getValue());
        sendToast(player, $toastType, $icon, title);

        return CommandHelper.Return.SUCCESS;
    }
}

