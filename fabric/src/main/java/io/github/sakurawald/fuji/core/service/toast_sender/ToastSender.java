package io.github.sakurawald.fuji.core.service.toast_sender;

import io.github.sakurawald.fuji.core.auxiliary.LogUtil;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.RegistryHelper;
import io.github.sakurawald.fuji.core.command.exception.AbortCommandExecutionException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementCriterion;
import net.minecraft.advancement.AdvancementDisplay;
import net.minecraft.advancement.AdvancementFrame;
import net.minecraft.advancement.AdvancementProgress;
import net.minecraft.advancement.AdvancementRewards;
import net.minecraft.advancement.criterion.CriterionProgress;
import net.minecraft.advancement.criterion.ImpossibleCriterion;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.AdvancementUpdateS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

#if MC_VER <= MC_1_20_1
import java.util.HashMap;
#elif MC_VER > MC_1_20_1
import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.advancement.AdvancementRequirements;
#endif


#if MC_VER >= MC_1_21_5
import net.minecraft.util.AssetInfo;
#endif

public class ToastSender {
    private static final String IMPOSSIBLE = "impossible";
    private static final String DUMMY_RESOURCE_IMAGE_IDENTIFIER = "minecraft:textures/gui/advancements/backgrounds/end.png";
    private static final Identifier SEND_TOAST_IDENTIFIER = Identifier.of("custom", "custom");

    public static void sendToast(@NotNull ServerPlayerEntity player, @NotNull AdvancementFrame advancementFrame, @NotNull ItemStack icon, @NotNull Text title) {
        /* Make an advancement display. */
        AdvancementDisplay advancementDisplay = new AdvancementDisplay(
            icon
            , title
            , Text.empty()
            ,
                #if MC_VER <= MC_1_20_2
                    RegistryHelper.makeIdentifierOrThrow(DUMMY_RESOURCE_IMAGE_IDENTIFIER)
                #elif MC_VER > MC_1_20_2 && MC_VER <= MC_1_21_4
                    Optional.of(RegistryHelper.makeIdentifierOrThrow(DUMMY_RESOURCE_IMAGE_IDENTIFIER))
                #elif MC_VER >= MC_1_21_5
                    Optional.of(makeDummyAssetInfo())
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

    private static
    #if MC_VER < MC_1_21_9
    AssetInfo
    #elif MC_VER >= MC_1_21_9
    AssetInfo.TextureAssetInfo
    #endif
    makeDummyAssetInfo() {
        #if MC_VER < MC_1_21_9
        return new AssetInfo(RegistryHelper.makeIdentifierOrThrow(DUMMY_RESOURCE_IMAGE_IDENTIFIER));
        #elif MC_VER >= MC_1_21_9
        return new AssetInfo.TextureAssetInfo(RegistryHelper.makeIdentifierOrThrow(DUMMY_RESOURCE_IMAGE_IDENTIFIER));
        #endif
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
        #if MC_VER <= MC_1_20_1
            List<String> collection = List.of(IMPOSSIBLE);
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
            LogUtil.error("It's strange that the statement `advancementProgress.getCriterionProgress(IMPOSSIBLE)` is null, aborting this advancement packet making.");
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

}
