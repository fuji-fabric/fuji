package mod.fuji.module.initializer.works.structure.work.impl;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import mod.fuji.core.auxiliary.ChronosUtil;
import mod.fuji.core.auxiliary.minecraft.EntityHelper;
import mod.fuji.core.auxiliary.minecraft.GuiHelper;
import mod.fuji.core.auxiliary.minecraft.LogicHelper;
import mod.fuji.core.auxiliary.minecraft.TextHelper;
import mod.fuji.core.auxiliary.minecraft.WorldHelper;
import mod.fuji.core.document.annotation.TestCase;
import mod.fuji.core.gui.component.gui.ConfirmSignGui;
import mod.fuji.core.gui.component.gui.InputSignGui;
import mod.fuji.core.job.interfaces.Schedulable;
import mod.fuji.module.initializer.works.WorksInitializer;
import mod.fuji.module.initializer.works.structure.WorkType;
import mod.fuji.module.initializer.works.structure.WorksBinding;
import mod.fuji.module.initializer.works.structure.work.abst.Work;
import lombok.NoArgsConstructor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.vehicle.MinecartHopper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.chunk.LevelChunk;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@TestCase(action = "Create a new production work and start the sample.", targets = {
    "See if the chunk iterator works."
    , "See if the hopper mixin works."
})
@NoArgsConstructor
public class ProductionWork extends Work implements Schedulable {

    public @NotNull Sample sample = new Sample();

    public ProductionWork(@NotNull ServerPlayer player, String name) {
        super(player, name);
    }

    @Override
    public @NotNull String getObjectTypeString() {
        return WorkType.ProductionWork.name();
    }

    private @NotNull List<Component> formatSampleCounter(ServerPlayer player) {
        List<Component> ret = new ArrayList<>();
        long currentTimeMS = System.currentTimeMillis();

        Stream<Map.Entry<String, Long>> sortedStream = this.sample.sampleCounter.entrySet().stream().sorted((entry1, entry2) -> entry2.getValue().compareTo(entry1.getValue()));

        sortedStream.forEach(entry -> {
            String key = entry.getKey();
            double rate = entry.getValue() * ((double) (3600 * 1000) / (Math.min(this.sample.sampleEndTimeMS, currentTimeMS) - this.sample.sampleStartTimeMS));

            Component text = TextHelper.getTextByKey(player, "works.production_work.prop.sample_counter.entry", entry.getValue(), rate);
            text = TextHelper.Replacer.replaceTextWithNamedArgument(text, "item", (matcher) -> Component.translatable(key));
            ret.add(text);
        });

        if (ret.isEmpty()) {
            ret.add(TextHelper.getTextByKey(player, "works.production_work.prop.sample_counter.empty"));
        }
        return ret;
    }

    @Override
    public @NotNull List<Component> ofLore(ServerPlayer player) {
        /* construct lore */
        List<Component> ret = super.ofLore(player);
        // note: hide sample info in lore if sample not exists
        if (this.sample.sampleStartTimeMS == 0) {
            ret.addAll(TextHelper.getTextListByKey(player, "works.production_work.sample.not_exists"));
            return ret;
        }

        ret.add(TextHelper.getTextByKey(player, "works.production_work.prop.sample_start_time", ChronosUtil.Formatter.formatDate(this.sample.sampleStartTimeMS)));
        ret.add(TextHelper.getTextByKey(player, "works.production_work.prop.sample_end_time", ChronosUtil.Formatter.formatDate(this.sample.sampleEndTimeMS)));
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
    protected @NotNull Item getDefaultEntityIcon() {
        return Items.REDSTONE;
    }

    public void openInputSampleDistanceGui(@NotNull ServerPlayer player) {
        new InputSignGui(player, TextHelper.getTextByKey(player, "works.production_work.prompt.input.sample_distance")) {
            @Override
            public void onClose() {
                int limit = WorksInitializer.config.model().sample_distance_limit;
                int current;
                try {
                    current = Integer.parseInt(this.getLine(0).getString());
                } catch (NumberFormatException e) {
                    TextHelper.sendTextByKey(player, "input.syntax.error");
                    return;
                }

                if (current > limit) {
                    TextHelper.sendTextByKey(player, "input.limit.error");
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
    public void openSpecializedSettingsGui(ServerPlayer player, @NotNull SimpleGui parentGui) {
        final SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x1, player, false);
        gui.setTitle(TextHelper.getTextByKey(player, "works.work.set.specialized_settings.title"));
        gui.addSlot(new GuiElementBuilder()
            .setItem(Items.CLOCK)
            .setName(TextHelper.getTextByKey(player, "works.production_work.set.sample"))
            .setLore(TextHelper.getTextListByKey(player, "works.production_work.set.sample.lore"))
            .setCallback(() -> new ConfirmSignGui(player) {
                    @Override
                    public void onConfirm() {
                        openInputSampleDistanceGui(player);
                    }
                }.open()
            )
        );
        gui.setSlot(8, GuiHelper.Button.makeBackButton(player).setCallback(parentGui::open)
        );

        gui.open();
    }

    private boolean isSampling() {
        return System.currentTimeMillis() < this.sample.sampleEndTimeMS;
    }

    private boolean insideSampleDistance(@NotNull BlockPos position, @NotNull BlockPos blockPos) {
        float deltaX = Math.abs(blockPos.getX() - position.getX());
        float deltaZ = Math.abs(blockPos.getZ() - position.getZ());
        return deltaX <= this.sample.sampleDistance && deltaZ <= this.sample.sampleDistance;
    }

    @SuppressWarnings("unused")
    private @NotNull String formatBlockPosList(@NotNull List<BlockPos> blockPosList) {
        StringBuilder sb = new StringBuilder();
        for (BlockPos blockPos : blockPosList) {
            sb.append("(").append(blockPos.getX()).append(",").append(blockPos.getY()).append(",").append(blockPos.getZ()).append(")").append(" ");
        }
        return sb.toString();
    }

    private int resolveHoppers(@NotNull ServerPlayer player) {
        // clear cache entry
        WorksBinding.unbind(this);

        // add cache entry
        int hopperBlockCount = 0;
        int minecartHopperCount = 0;
        ServerLevel world = EntityHelper.getServerWorld(player);

        for (ChunkHolder chunkHolder : WorldHelper.getChunks(world)) {
            LevelChunk worldChunk = chunkHolder.getTickingChunk();
            if (worldChunk == null) continue;
            /* count for block entities */
            for (BlockEntity blockEntity : worldChunk.getBlockEntities().values()) {
                // improve: check type first for performance
                if (blockEntity instanceof HopperBlockEntity) {
                    if (insideSampleDistance(player.blockPosition(), blockEntity.getBlockPos())) {
                        WorksBinding.bind(blockEntity.getBlockPos(), this);
                        hopperBlockCount++;
                    }
                }
            }
        }
        for (Entity entity : WorldHelper.getEntities(world)) {
            if (entity instanceof MinecartHopper) {
                if (insideSampleDistance(player.blockPosition(), entity.blockPosition())) {
                    WorksBinding.bind(entity.getId(), this);
                    minecartHopperCount++;
                }
            }
        }

        TextHelper.sendTextByKey(player, "works.production_work.sample.resolve_hoppers.response", hopperBlockCount, minecartHopperCount);
        return hopperBlockCount + minecartHopperCount;
    }

    @Override
    public void onSchedule() {
        if (System.currentTimeMillis() >= this.sample.sampleEndTimeMS) {
            this.endSample();
        }
    }

    private void startSample(@NotNull ServerPlayer player) {
        this.sample.sampleStartTimeMS = System.currentTimeMillis();
        this.sample.sampleEndTimeMS = this.sample.sampleStartTimeMS + WorksInitializer.config.model().sample_time_ms;
        this.sample.sampleDimension = EntityHelper.getServerWorld(player).dimension().location().toString();
        this.sample.sampleX = player.getX();
        this.sample.sampleY = player.getY();
        this.sample.sampleZ = player.getZ();
        this.sample.sampleCounter = new HashMap<>();

        LogicHelper.withCancelCheck(player, this.resolveHoppers(player) == 0, () -> {
            TextHelper.sendBroadcastByKey("works.production_work.sample.start", name, this.creator);
        });
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
        String key = itemStack.getItem().getDescriptionId();
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
