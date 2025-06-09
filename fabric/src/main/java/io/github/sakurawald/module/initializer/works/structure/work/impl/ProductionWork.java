package io.github.sakurawald.module.initializer.works.structure.work.impl;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import io.github.sakurawald.core.auxiliary.ChronosUtil;
import io.github.sakurawald.core.auxiliary.minecraft.GuiHelper;
import io.github.sakurawald.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.core.gui.ConfirmGui;
import io.github.sakurawald.core.gui.InputSignGui;
import io.github.sakurawald.module.initializer.works.WorksInitializer;
import io.github.sakurawald.module.initializer.works.structure.WorksBinding;
import io.github.sakurawald.module.initializer.works.structure.work.abst.Work;
import lombok.NoArgsConstructor;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.HopperBlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.vehicle.HopperMinecartEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ChunkHolder;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.WorldChunk;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@NoArgsConstructor

public class ProductionWork extends Work {

    public @NotNull Sample sample = new Sample();

    public ProductionWork(@NotNull ServerPlayerEntity player, String name) {
        super(player, name);
    }

    @Override
    protected @NotNull String getType() {
        return Work.WorkType.ProductionWork.name();
    }

    private @NotNull List<Text> formatSampleCounter(ServerPlayerEntity player) {
        List<Text> ret = new ArrayList<>();
        long currentTimeMS = System.currentTimeMillis();

        Stream<Map.Entry<String, Long>> sortedStream = this.sample.sampleCounter.entrySet().stream().sorted((entry1, entry2) -> entry2.getValue().compareTo(entry1.getValue()));

        sortedStream.forEach(entry -> {
            String key = entry.getKey();
            double rate = entry.getValue() * ((double) (3600 * 1000) / ((Math.min(this.sample.sampleEndTimeMS, currentTimeMS)) - this.sample.sampleStartTimeMS));

            Text text = TextHelper.getTextByKey(player, "works.production_work.prop.sample_counter.entry", entry.getValue(), rate);
            text = TextHelper.replaceTextWithMarker(text, "item", ()->Text.translatable(key));
            ret.add(text);
        });

        if (ret.isEmpty()) {
            ret.add(TextHelper.getTextByKey(player, "works.production_work.prop.sample_counter.empty"));
        }
        return ret;
    }

    @Override
    public @NotNull List<Text> asLore(ServerPlayerEntity player) {
        /* construct lore */
        List<Text> ret = super.asLore(player);
        // note: hide sample info in lore if sample not exists
        if (this.sample.sampleStartTimeMS == 0) {
            ret.addAll(TextHelper.getTextListByKey(player, "works.production_work.sample.not_exists"));
            return ret;
        }

        ret.add(TextHelper.getTextByKey(player, "works.production_work.prop.sample_start_time", ChronosUtil.toStandardDateFormat(this.sample.sampleStartTimeMS)));
        ret.add(TextHelper.getTextByKey(player, "works.production_work.prop.sample_end_time", ChronosUtil.toStandardDateFormat(this.sample.sampleEndTimeMS)));
        ret.add(TextHelper.getTextByKey(player, "works.production_work.prop.sample_dimension", this.sample.sampleDimension));
        ret.add(TextHelper.getTextByKey(player, "works.production_work.prop.sample_coordinate", this.sample.sampleX, this.sample.sampleY, this.sample.sampleZ));
        ret.add(TextHelper.getTextByKey(player, "works.production_work.prop.sample_distance", this.sample.sampleDistance));

        // check npe to avoid broken
        if (this.sample.sampleCounter != null) {
            // trim counter
            if (this.sample.sampleCounter.size() > WorksInitializer.config.model().sample_counter_top_n) {
                trimCounter();
            }
            ret.add(TextHelper.getTextByKey(player, "works.production_work.prop.sample_counter"));
            ret.addAll(formatSampleCounter(player));
        }
        return ret;
    }

    @Override
    protected @NotNull String getDefaultIconItemIdentifier() {
        return "minecraft:redstone";
    }

    public void openInputSampleDistanceGui(@NotNull ServerPlayerEntity player) {
        new InputSignGui(player, TextHelper.getTextByKey(player, "works.production_work.prompt.input.sample_distance")) {
            @Override
            public void onClose() {
                int limit = WorksInitializer.config.model().sample_distance_limit;
                int current;
                try {
                    current = Integer.parseInt(this.getLine(0).getString());
                } catch (NumberFormatException e) {
                    TextHelper.sendActionBarByKey(player, "input.syntax.error");
                    return;
                }

                if (current > limit) {
                    TextHelper.sendActionBarByKey(player, "input.limit.error");
                    return;
                }

                // set sample distance
                sample.sampleDistance = current;

                // start/restart sample
                if (isSampling()) {
                    endSample();
                }
                startSample(player);
            }
        }.open();
    }

    @Override
    public void openSpecializedSettingsGui(ServerPlayerEntity player, @NotNull SimpleGui parentGui) {
        final SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X1, player, false);
        gui.setTitle(TextHelper.getTextByKey(player, "works.work.set.specialized_settings.title"));
        gui.setLockPlayerInventory(true);
        gui.addSlot(new GuiElementBuilder()
            .setItem(Items.CLOCK)
            .setName(TextHelper.getTextByKey(player, "works.production_work.set.sample"))
            .setLore(TextHelper.getTextListByKey(player, "works.production_work.set.sample.lore"))
            .setCallback(() -> new ConfirmGui(player) {
                    @Override
                    public void onConfirm() {
                        openInputSampleDistanceGui(player);
                    }
                }.open()
            )
        );
        gui.setSlot(8, GuiHelper.makeBackButton(player).setCallback(parentGui::open)
        );

        gui.open();
    }

    private boolean isSampling() {
        return System.currentTimeMillis() < this.sample.sampleEndTimeMS;
    }

    @SuppressWarnings("EmptyMethod")
    @Override
    public Item getIconItem() {
        return super.getIconItem();
    }

    private boolean insideSampleDistance(@NotNull BlockPos position, @NotNull BlockPos blockPos) {
        float deltaX = Math.abs(blockPos.getX() - position.getX());
        float deltaZ = Math.abs(blockPos.getZ() - position.getZ());
        return deltaX <= this.sample.sampleDistance && deltaZ <= this.sample.sampleDistance;
    }

    @SuppressWarnings("unused")
    private @NotNull String formatBlockPosList(@NotNull ArrayList<BlockPos> blockPosList) {
        StringBuilder sb = new StringBuilder();
        for (BlockPos blockPos : blockPosList) {
            sb.append("(").append(blockPos.getX()).append(",").append(blockPos.getY()).append(",").append(blockPos.getZ()).append(")").append(" ");
        }
        return sb.toString();
    }

    private int resolveHoppers(@NotNull ServerPlayerEntity player) {
        // clear cache entry
        WorksBinding.unbind(this);

        // add cache entry
        int hopperBlockCount = 0;
        int minecartHopperCount = 0;
        ServerWorld world = player.getServerWorld();

        Iterable<ChunkHolder> chunkHolders = world.getChunkManager().chunkLoadingManager.entryIterator();
        for (ChunkHolder chunkHolder : chunkHolders) {
            WorldChunk worldChunk = chunkHolder.getWorldChunk();
            if (worldChunk == null) continue;
            /* count for block entities */
            for (BlockEntity blockEntity : worldChunk.getBlockEntities().values()) {
                // improve: check type first for performance
                if (blockEntity instanceof HopperBlockEntity) {
                    if (insideSampleDistance(player.getBlockPos(), blockEntity.getPos())) {
                        WorksBinding.bind(blockEntity.getPos(), this);
                        hopperBlockCount++;
                    }
                }
            }
        }
        for (Entity entity : world.iterateEntities()) {
            if (entity instanceof HopperMinecartEntity) {
                if (insideSampleDistance(player.getBlockPos(), entity.getBlockPos())) {
                    WorksBinding.bind(entity.getId(), this);
                    minecartHopperCount++;
                }
            }
        }

        TextHelper.sendMessageByKey(player, "works.production_work.sample.resolve_hoppers.response", hopperBlockCount, minecartHopperCount);
        return hopperBlockCount + minecartHopperCount;
    }

    @Override
    public void onSchedule() {
        if (System.currentTimeMillis() >= this.sample.sampleEndTimeMS) {
            this.endSample();
        }
    }

    private void startSample(@NotNull ServerPlayerEntity player) {
        this.sample.sampleStartTimeMS = System.currentTimeMillis();
        this.sample.sampleEndTimeMS = this.sample.sampleStartTimeMS + WorksInitializer.config.model().sample_time_ms;
        this.sample.sampleDimension = player.getServerWorld().getRegistryKey().getValue().toString();
        this.sample.sampleX = player.getX();
        this.sample.sampleY = player.getY();
        this.sample.sampleZ = player.getZ();
        this.sample.sampleCounter = new HashMap<>();
        if (this.resolveHoppers(player) == 0) {
            TextHelper.sendMessageByKey(player, "operation.cancelled");
            return;
        }

        TextHelper.sendBroadcastByKey("works.production_work.sample.start", name, this.creator);
    }

    private void endSample() {
        // unbind all block pos
        WorksBinding.unbind(this);
        TextHelper.sendBroadcastByKey("works.production_work.sample.end", this.name, this.creator);

        // trim counter to avoid spam
        trimCounter();
    }

    private void trimCounter() {
        List<Map.Entry<String, Long>> sortedEntries = this.sample.sampleCounter.entrySet()
            .stream()
            .sorted((entry1, entry2) -> entry2.getValue().compareTo(entry1.getValue()))
            .toList();

        int N = WorksInitializer.config.model().sample_counter_top_n;
        this.sample.sampleCounter.clear();
        for (int i = 0; i < N && i < sortedEntries.size(); i++) {
            this.sample.sampleCounter.put(sortedEntries.get(i).getKey(), sortedEntries.get(i).getValue());
        }
    }

    public void addCounter(@NotNull ItemStack itemStack) {
        HashMap<String, Long> counter = this.sample.sampleCounter;
        String key = itemStack.getItem().getTranslationKey();
        counter.put(key, counter.getOrDefault(key, 0L) + itemStack.getCount());
    }

    public static class Sample {
        public String sampleDimension;
        public double sampleX;
        public double sampleY;
        public double sampleZ;
        public long sampleStartTimeMS;
        public long sampleEndTimeMS;
        public int sampleDistance;
        public HashMap<String, Long> sampleCounter;
    }
}
