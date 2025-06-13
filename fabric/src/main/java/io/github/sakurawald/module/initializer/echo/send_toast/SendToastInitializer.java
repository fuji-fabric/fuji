package io.github.sakurawald.module.initializer.echo.send_toast;

import io.github.sakurawald.core.auxiliary.LogUtil;
import io.github.sakurawald.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.core.auxiliary.minecraft.RegistryHelper;
import io.github.sakurawald.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.core.command.annotation.CommandNode;
import io.github.sakurawald.core.command.annotation.CommandRequirement;
import io.github.sakurawald.core.command.annotation.CommandSource;
import io.github.sakurawald.core.command.argument.wrapper.impl.GreedyString;
import io.github.sakurawald.core.command.exception.AbortCommandExecutionException;
import io.github.sakurawald.module.initializer.ModuleInitializer;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementCriterion;
import net.minecraft.advancement.AdvancementDisplay;
import net.minecraft.advancement.AdvancementFrame;
import net.minecraft.advancement.AdvancementProgress;

#if MC_VER <= MC_1_20_1
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


public class SendToastInitializer extends ModuleInitializer {
    private static final String IMPOSSIBLE = "impossible";
    public static final String DUMMY_RESOURCE_IMAGE_IDENTIFIER = "minecraft:textures/gui/advancements/backgrounds/end.png";

    #if MC_VER <= MC_1_20_1
    #elif MC_VER > MC_1_20_1

    private static void sendToast(ServerPlayerEntity player, AdvancementFrame advancementFrame, Item icon, Text title) {
        AdvancementDisplay advancementDisplay = new AdvancementDisplay(
            icon.getDefaultStack()
            , title
            , Text.empty()
            , Optional.of(
                #if MC_VER <= MC_1_20_2
            RegistryHelper.makeIdentifier(DUMMY_RESOURCE_IMAGE_IDENTIFIER)
                #elif MC_VER > MC_1_20_2 && MC_VER <= MC_1_21_4
                    Optional.of(RegistryHelper.makeIdentifier(DUMMY_RESOURCE_IMAGE_IDENTIFIER))
                #elif MC_VER >= MC_1_21_5
                    new AssetInfo(RegistryHelper.makeIdentifier(DUMMY_RESOURCE_IMAGE_IDENTIFIER))
                #endif
        )

            , advancementFrame
            , true
            , false
            , true
        );
        Identifier identifier = Identifier.of("custom", "custom");

        ImpossibleCriterion criterion = new ImpossibleCriterion();
        AdvancementCriterion<ImpossibleCriterion.Conditions> conditionsAdvancementCriterion = criterion.create(new ImpossibleCriterion.Conditions());

        AdvancementEntry advancementEntry = Advancement.Builder.create()
            .display(advancementDisplay)
            .rewards(AdvancementRewards.NONE)
            .requirements(AdvancementRequirements.anyOf(List.of(IMPOSSIBLE)))
            .criterion(IMPOSSIBLE, conditionsAdvancementCriterion)
            .build(identifier);

        player.networkHandler.sendPacket(makeGrantPacket(advancementEntry, identifier));
        player.networkHandler.sendPacket(makeRevokePacket(identifier));
    }

    private static @NotNull AdvancementUpdateS2CPacket makeGrantPacket(AdvancementEntry advancementEntry, Identifier identifier) {
        AdvancementProgress advancementProgress = new AdvancementProgress();

        #if MC_VER <= MC_1_20_2
            String[][] impossible = {{IMPOSSIBLE}};
            AdvancementRequirements advancementRequirements = new AdvancementRequirements(impossible);
        #elif MC_VER > MC_1_20_2
            AdvancementRequirements advancementRequirements = new AdvancementRequirements(List.of(List.of(IMPOSSIBLE)));
        #endif

        advancementProgress.init(advancementRequirements);

        CriterionProgress criterionProgress = advancementProgress.getCriterionProgress(IMPOSSIBLE);
        if (criterionProgress == null) {
            LogUtil.error("It's strange that the statement `advancementProgress.getCriterionProgress(IMPOSSIBLE) is null, abort this advancement packet making.`");
            throw new AbortCommandExecutionException();
        }

        criterionProgress.obtain();

        Collection<AdvancementEntry> toEarn = List.of(advancementEntry);
        Set<Identifier> toRemove = Set.of();
        Map<Identifier, AdvancementProgress> toSetProgress = Map.of(identifier, advancementProgress);
        return makeAdvancementUpdatePacket(toEarn,toRemove,toSetProgress);
    }

    private static AdvancementUpdateS2CPacket makeAdvancementUpdatePacket(Collection<AdvancementEntry> toEarn, Set<Identifier> toRemove, Map<Identifier, AdvancementProgress> toSetProgress) {
        #if MC_VER <= MC_1_21_4
        return new AdvancementUpdateS2CPacket(false, toEarn, toRemove, toSetProgress);
        #elif MC_VER >= MC_1_21_5
            return new AdvancementUpdateS2CPacket(false, toEarn, toRemove, toSetProgress, true);
        #endif
    }

    private static @NotNull AdvancementUpdateS2CPacket makeRevokePacket(Identifier identifier) {
        Collection<AdvancementEntry> toEarn = List.of();
        Set<Identifier> toRemove = Set.of(identifier);
        Map<Identifier, AdvancementProgress> toSetProgress = Map.of();
        return makeAdvancementUpdatePacket(toEarn, toRemove, toSetProgress);
    }

    @CommandNode("send-toast")
    @CommandRequirement(level = 4)
    private static int sendToast(@CommandSource ServerCommandSource source
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
    #endif
}

