package mod.fuji.core.service.toast_sender;

import mod.fuji.core.auxiliary.LogUtil;
import mod.fuji.core.auxiliary.minecraft.RegistryHelper;
import mod.fuji.core.command.exception.AbortCommandExecutionException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.CriterionProgress;
import net.minecraft.advancements.critereon.ImpossibleTrigger;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.protocol.game.ClientboundUpdateAdvancementsPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

#if MC_VER <= MC_1_20_1
import java.util.HashMap;
#elif MC_VER > MC_1_20_1
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementRequirements;
#endif


public class ToastSender {
    private static final String IMPOSSIBLE = "impossible";
    private static final String DUMMY_RESOURCE_IMAGE_IDENTIFIER = "minecraft:textures/gui/advancements/backgrounds/end.png";
    private static final ResourceLocation SEND_TOAST_IDENTIFIER = ResourceLocation.fromNamespaceAndPath("custom", "custom");

    public static void sendToast(@NotNull ServerPlayer player, @NotNull AdvancementType advancementFrame, @NotNull ItemStack icon, @NotNull Component title) {
        /* Make an advancement display. */
        DisplayInfo advancementDisplay = new DisplayInfo(
            icon
            , title
            , Component.empty()
            ,
                #if MC_VER <= MC_1_20_2
                    RegistryHelper.makeIdentifierOrThrow(DUMMY_RESOURCE_IMAGE_IDENTIFIER)
                #elif MC_VER > MC_1_20_2 && MC_VER <= MC_1_21_4
                    Optional.of(RegistryHelper.makeIdentifierOrThrow(DUMMY_RESOURCE_IMAGE_IDENTIFIER))
                #elif MC_VER >= MC_1_21_5 && MC_VER < MC_1_21_9
                    Optional.of(new net.minecraft.core.ClientAsset(RegistryHelper.makeIdentifierOrThrow(DUMMY_RESOURCE_IMAGE_IDENTIFIER)))
                #elif MC_VER >= MC_1_21_9
                    Optional.of(new net.minecraft.core.ClientAsset.ResourceTexture(RegistryHelper.makeIdentifierOrThrow(DUMMY_RESOURCE_IMAGE_IDENTIFIER)))
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
        AdvancementHolder advancementEntry
        #endif
            = Advancement.Builder
            .advancement()
            .display(advancementDisplay)
            .rewards(AdvancementRewards.EMPTY)
            .requirements(makeAdvancementRequirements())
            .addCriterion(IMPOSSIBLE, makeAdvancementCriterion())
            .build(SEND_TOAST_IDENTIFIER);

        /* Send packets. */
        player.connection.send(makeGrantPacket(advancementEntry, SEND_TOAST_IDENTIFIER));
        player.connection.send(makeRevokePacket(SEND_TOAST_IDENTIFIER));
    }

    @SuppressWarnings("UnnecessaryLocalVariable")
    private static
    #if MC_VER <= MC_1_20_1
    AdvancementCriterion
    #elif MC_VER > MC_1_20_1
    Criterion<ImpossibleTrigger.TriggerInstance>
    #endif
    makeAdvancementCriterion() {
        #if MC_VER <= MC_1_20_1
            AdvancementCriterion advancementCriterion = new AdvancementCriterion(new ImpossibleCriterion.Conditions());
        #elif MC_VER > MC_1_20_1
        Criterion<ImpossibleTrigger.TriggerInstance> advancementCriterion = new ImpossibleTrigger().createCriterion(new ImpossibleTrigger.TriggerInstance());
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
        advancementProgress.update(makeAdvancementRequirements());
        #endif

        return advancementProgress;
    }

    @SuppressWarnings("SameParameterValue")
    private static @NotNull ClientboundUpdateAdvancementsPacket makeGrantPacket(
        #if MC_VER <= MC_1_20_1
        Advancement advancementEntry
        #elif MC_VER > MC_1_20_1
            AdvancementHolder advancementEntry
        #endif
        , ResourceLocation identifier) {

        /* Make advancement progress. */
        AdvancementProgress advancementProgress = makeAdvancementProgress();

        /* Call obtain for criterion progress. */
        CriterionProgress criterionProgress = advancementProgress.getCriterion(IMPOSSIBLE);
        if (criterionProgress == null) {
            LogUtil.error("It's strange that the statement `advancementProgress.getCriterionProgress(IMPOSSIBLE)` is null, aborting this advancement packet making.");
            throw new AbortCommandExecutionException();
        }
        criterionProgress.grant();

        /* Send the packet. */
        #if MC_VER <= MC_1_20_1
        Collection<Advancement> toEarn = List.of(advancementEntry);
        #elif MC_VER > MC_1_20_1
        Collection<AdvancementHolder> toEarn = List.of(advancementEntry);
        #endif

        Set<ResourceLocation> toRemove = Set.of();
        Map<ResourceLocation, AdvancementProgress> toSetProgress = Map.of(identifier, advancementProgress);
        return makeAdvancementUpdatePacket(toEarn, toRemove, toSetProgress);
    }

    private static ClientboundUpdateAdvancementsPacket makeAdvancementUpdatePacket(
        #if MC_VER <= MC_1_20_1
        Collection<Advancement> toEarn
        #elif MC_VER > MC_1_20_1
            Collection<AdvancementHolder> toEarn
        #endif
        , Set<ResourceLocation> toRemove, Map<ResourceLocation, AdvancementProgress> toSetProgress)
    {
        #if MC_VER <= MC_1_21_4
            return new AdvancementUpdateS2CPacket(false, toEarn, toRemove, toSetProgress);
        #elif MC_VER >= MC_1_21_5
        return new ClientboundUpdateAdvancementsPacket(false, toEarn, toRemove, toSetProgress, true);
        #endif
    }

    @SuppressWarnings("SameParameterValue")
    private static @NotNull ClientboundUpdateAdvancementsPacket makeRevokePacket(ResourceLocation identifier) {
        #if MC_VER <= MC_1_20_1
            Collection<Advancement> toEarn = List.of();
        #elif MC_VER > MC_1_20_1
        Collection<AdvancementHolder> toEarn = List.of();
        #endif

        Set<ResourceLocation> toRemove = Set.of(identifier);
        Map<ResourceLocation, AdvancementProgress> toSetProgress = Map.of();
        return makeAdvancementUpdatePacket(toEarn, toRemove, toSetProgress);
    }

}
