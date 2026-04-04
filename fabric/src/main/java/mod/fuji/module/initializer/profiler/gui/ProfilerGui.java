package mod.fuji.module.initializer.profiler.gui;

import eu.pb4.sgui.api.elements.GuiElement;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryType;
import java.lang.management.MemoryUsage;
import java.nio.file.FileStore;
import java.nio.file.FileSystems;
import java.util.ArrayList;
import java.util.List;
import mod.fuji.core.auxiliary.StringUtil;
import mod.fuji.core.auxiliary.minecraft.GuiHelper;
import mod.fuji.core.auxiliary.minecraft.RegistryHelper;
import mod.fuji.core.auxiliary.minecraft.TextHelper;
import mod.fuji.core.auxiliary.minecraft.WorldHelper;
import mod.fuji.core.gui.structure.GuiElementIR;
import mod.fuji.module.initializer.profiler.ProfilerInitializer;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.chunk.LevelChunk;

public class ProfilerGui extends SimpleGui {

    private static final int LINE_SIZE = 9;
    private static final int CHUNK_AREA = (int) Math.pow(17.0D, 2.0D);

    public ProfilerGui(ServerPlayer player) {
        super(MenuType.GENERIC_9x6, player, false);

        this.setTitle(TextHelper.getTextByKey(getPlayer(), "profiler.gui.title"));

        GuiHelper.setSlot(this, 0, makeOperatingSystemElement());
        GuiHelper.setSlot(this, 1, makeVirtualMachineElement());

        GuiHelper.setSlot(this, 3, makeCpuElement());
        GuiHelper.setSlot(this, 4, makeTpsElement());
        GuiHelper.setSlot(this, 5, makeMsptElement());

        List<GuiElementIR> dimensionElements = makeDimensionElements();
        int dimensionElementsOffset = dimensionElements.size() >= LINE_SIZE ? 0 : LINE_SIZE;
        for (int i = 0; i < dimensionElements.size(); i++) {
            GuiHelper.setSlot(this, LINE_SIZE + dimensionElementsOffset + i, dimensionElements.get(i));
        }

        List<GuiElementIR> fileSystemElements = makeFileSystemElements();
        for (int i = 0; i < fileSystemElements.size(); i++) {
            GuiHelper.setSlot(this, LINE_SIZE * 3 + i, fileSystemElements.get(i));
        }

        List<GuiElementIR> memoryElements = makeMemoryElements();
        for (int i = 0; i < memoryElements.size(); i++) {
            GuiHelper.setSlot(this, LINE_SIZE * 4 + i, memoryElements.get(i));
        }

        List<GuiElementIR> gcElements = makeGcElements();
        for (int i = 0; i < gcElements.size(); i++) {
            GuiHelper.setSlot(this, LINE_SIZE * 5 + i, gcElements.get(i));
        }
    }

    private List<GuiElementIR> makeDimensionElements() {
        List<GuiElementIR> elements = new ArrayList<>();

        for (ServerLevel world : WorldHelper.getWorlds()) {
            List<Component> lore = new ArrayList<>();

            /* Dimension name. */
            lore.add(TextHelper.getTextByKey(getPlayer(), "profiler.dimension.name", RegistryHelper.getIdAsString(world)));

            /* Block entities. */
            int blockEntityCount = 0;
            for (ChunkHolder chunk : WorldHelper.getChunks(world)) {
                LevelChunk worldChunk = chunk.getTickingChunk();
                if (worldChunk == null) continue;
                blockEntityCount += worldChunk.getBlockEntities().size();
            }
            lore.add(TextHelper.getTextByKey(getPlayer(), "profiler.dimension.block_entities", blockEntityCount));

            /* Spawn info. */
            NaturalSpawner.SpawnState spawnInfo = world.getChunkSource().getLastSpawnState();
            if (spawnInfo != null) {
                spawnInfo.getMobCategoryCounts().forEach((k, v) -> {
                    String groupName = k.getName();
                    int groupCount = v;
                    int groupCapacity = k.getMaxInstancesPerChunk() * spawnInfo.getSpawnableChunkCount() / CHUNK_AREA;

                    lore.add(TextHelper.getTextByKey(getPlayer(), "profiler.dimension.entity_group", groupName, groupCount, groupCapacity));
                });

            }

            var element = GuiElementIR.of(new GuiElementBuilder()
                .setItem(WorldHelper.toGuiItem(RegistryHelper.getIdAsString(world)))
                .setName(TextHelper.getTextByKey(getPlayer(), "profiler.dimension"))
                .setLore(lore)
                .build());
            elements.add(element);
        }

        return elements;
    }

    private GuiElementIR makeOperatingSystemElement() {
        String osName = ManagementFactory.getOperatingSystemMXBean().getName();
        String osVersion = ManagementFactory.getOperatingSystemMXBean().getVersion();
        String osArch = ManagementFactory.getOperatingSystemMXBean().getArch();

        return GuiElementIR.of(new GuiElementBuilder()
            .setItem(Items.SLIME_BLOCK)
            .setName(TextHelper.getTextByKey(getPlayer(), "profiler.os"))
            .setLore(List.of(
                TextHelper.getTextByKey(getPlayer(), "profiler.os.name", osName)
                , TextHelper.getTextByKey(getPlayer(), "profiler.os.version", osVersion)
                , TextHelper.getTextByKey(getPlayer(), "profiler.os.arch", osArch)
            ))
            .build());
    }

    private GuiElementIR makeVirtualMachineElement() {
        String vmName = ManagementFactory.getRuntimeMXBean().getVmName();
        String vmVersion = ManagementFactory.getRuntimeMXBean().getVmVersion();

        return GuiElementIR.of(new GuiElementBuilder()
            .setItem(Items.HONEY_BLOCK)
            .setName(TextHelper.getTextByKey(getPlayer(), "profiler.vm"))
            .setLore(List.of(
                TextHelper.getTextByKey(getPlayer(), "profiler.vm.name", vmName)
                , TextHelper.getTextByKey(getPlayer(), "profiler.vm.version", vmVersion)
            ))
            .build());
    }

    private GuiElementIR makeTpsElement() {
        return GuiElementIR.of(new GuiElementBuilder()
            .setItem(Items.CLOCK)
            .setName(TextHelper.getTextByKey(getPlayer(), "profiler.tps"))
            .setLore(List.of(
                TextHelper.getTextByKey(getPlayer(), "profiler.tps.description")
                , TextHelper.getTextByKey(getPlayer(), "profiler.tps.avg.5s")
                , TextHelper.getTextByKey(getPlayer(), "profiler.tps.avg.10s")
                , TextHelper.getTextByKey(getPlayer(), "profiler.tps.avg.1m")
                , TextHelper.getTextByKey(getPlayer(), "profiler.tps.avg.5m")
                , TextHelper.getTextByKey(getPlayer(), "profiler.tps.avg.15m")
            ))
            .build());
    }

    private GuiElementIR makeMsptElement() {
        return GuiElementIR.of(new GuiElementBuilder()
            .setItem(Items.CLOCK)
            .setName(TextHelper.getTextByKey(getPlayer(), "profiler.mspt"))
            .setLore(List.of(
                TextHelper.getTextByKey(getPlayer(), "profiler.mspt.description")
                , TextHelper.getTextByKey(getPlayer(), "profiler.mspt.summary.10s")
                , TextHelper.getTextByKey(getPlayer(), "profiler.mspt.summary.1m")
            ))
            .build());
    }

    private GuiElementIR makeCpuElement() {
        return GuiElementIR.of(new GuiElementBuilder()
            .setItem(Items.COMPARATOR)
            .setName(TextHelper.getTextByKey(getPlayer(), "profiler.cpu"))
            .setLore(List.of(
                TextHelper.getTextByKey(getPlayer(), "profiler.cpu.description")
                , TextHelper.getTextByKey(getPlayer(), "profiler.cpu.usage.system")
                , TextHelper.getTextByKey(getPlayer(), "profiler.cpu.usage.user")
            ))
            .build());
    }

    private List<GuiElementIR> makeFileSystemElements() {
        List<GuiElementIR> elements = new ArrayList<>();

        /* Make file store list. */
        List<FileStore> fileStores = new ArrayList<>();
        for (FileStore fileStore : FileSystems.getDefault().getFileStores()) {
            try {
                /* Filter file store. */
                if (fileStore.getTotalSpace() == 0) continue;
                if (ProfilerInitializer.config.model().fileSystem.blacklisted_filesystem.stream().anyMatch(nameRegex -> fileStore.toString().matches(nameRegex)))
                    continue;

                /* Add the file stores. */
                fileStores.add(fileStore);
            } catch (Exception ignore) {
                // Okay, no permission for that file.
            }
        }

        /* Sort the file store by used space. */
        fileStores = fileStores.stream()
            .sorted((a, b) -> {
                try {
                    long aUsed = a.getTotalSpace() - a.getUsableSpace();
                    long bUsed = b.getTotalSpace() - b.getUsableSpace();
                    return Long.compare(bUsed, aUsed);
                } catch (Exception ignore) {
                    // Okay, no permission for that file.
                }

                return 0;
            })
            .toList();

        /* Make GUI elements. */
        for (FileStore fileStore : fileStores) {
            try {
                /* Add the file store. */
                long maxSpace = fileStore.getTotalSpace();
                long usedSpace = fileStore.getTotalSpace() - fileStore.getUsableSpace();
                double usagePercent = (double) usedSpace / maxSpace;

                GuiElementBuilder builder = new GuiElementBuilder()
                    .setItem(Items.BOOKSHELF)
                    .setName(TextHelper.getTextByKey(getPlayer(), "profiler.fs"))
                    .setLore(List.of(
                        TextHelper.getTextByKey(getPlayer(), "profiler.fs.name", fileStore)
                        , TextHelper.getTextByKey(getPlayer(), "profiler.fs.type", fileStore.type())
                        , TextHelper.getTextByKey(getPlayer(), "profiler.fs.max", StringUtil.formatBytes(maxSpace))
                        , TextHelper.getTextByKey(getPlayer(), "profiler.fs.used", StringUtil.formatBytes(usedSpace))
                        , TextHelper.getTextByKey(getPlayer(), "profiler.fs.usage", usagePercent)
                    ));

                elements.add(GuiElementIR.of(builder.build()));
            } catch (Exception ignore) {
                // Okay, no permission for that file.
            }
        }

        return elements;
    }

    private List<GuiElementIR> makeMemoryElements() {
        List<GuiElementIR> elements = new ArrayList<>();

        /* Get memory pools. */
        List<MemoryPoolMXBean> memoryPoolMXBeans = ManagementFactory.getMemoryPoolMXBeans();
        memoryPoolMXBeans.sort((a, b) -> {
            /* Heap memory first. */
            if (a.getType() == MemoryType.HEAP && b.getType() != MemoryType.HEAP) {
                return -1;
            }

            if (b.getType() == MemoryType.HEAP && a.getType() != MemoryType.HEAP) {
                return 1;
            }

            /* Large memory first. */
            if (a.getUsage().getUsed() > b.getUsage().getUsed()) {
                return -1;
            } else if (b.getUsage().getUsed() > a.getUsage().getUsed()) {
                return 1;
            }

            return 0;
        });

        /* Convert memory pool into GUI elements. */
        for (MemoryPoolMXBean memoryPoolMXBean : memoryPoolMXBeans) {
            String memoryName = memoryPoolMXBean.getName();
            MemoryType memoryType = memoryPoolMXBean.getType();

            // NOTE: init <= used / committed <= max
            MemoryUsage memoryUsage = memoryPoolMXBean.getUsage();
            String init = StringUtil.formatBytes(memoryUsage.getInit());
            String used = StringUtil.formatBytes(memoryUsage.getUsed());
            String committed = StringUtil.formatBytes(memoryUsage.getCommitted());
            String max = StringUtil.formatBytes(memoryUsage.getMax());

            Item item;
            if (memoryType == MemoryType.HEAP) {
                item = Items.ENCHANTED_BOOK;
            } else {
                item = Items.BOOK;
            }

            GuiElementBuilder guiElementBuilder = new GuiElementBuilder()
                .setItem(item)
                .setName(TextHelper.getTextByKey(getPlayer(), "profiler.memory"))
                .setLore(List.of(
                    TextHelper.getTextByKey(getPlayer(), "profiler.memory.name", memoryName)
                    , TextHelper.getTextByKey(getPlayer(), "profiler.memory.type", memoryType)
                    , TextHelper.getTextByKey(getPlayer(), "profiler.memory.init", init)
                    , TextHelper.getTextByKey(getPlayer(), "profiler.memory.used", used)
                    , TextHelper.getTextByKey(getPlayer(), "profiler.memory.committed", committed)
                    , TextHelper.getTextByKey(getPlayer(), "profiler.memory.max", max)
                ));

            elements.add(GuiElementIR.of(guiElementBuilder.build()));
        }

        return elements;
    }

    private List<GuiElementIR> makeGcElements() {
        List<GuiElementIR> elements = new ArrayList<>();

        List<GarbageCollectorMXBean> gcMXBeans = ManagementFactory.getGarbageCollectorMXBeans();
        long uptime = ManagementFactory.getRuntimeMXBean().getUptime();
        for (GarbageCollectorMXBean gcMXBean : gcMXBeans) {
            String name = gcMXBean.getName();
            long totalGcTime = gcMXBean.getCollectionTime();
            long totalGcCount = gcMXBean.getCollectionCount();
            double avgFrequency = (double) uptime / totalGcCount / 1000;
            double avgTime = (double) totalGcTime / totalGcCount;

            GuiElement element = new GuiElementBuilder()
                .setItem(Items.CAMPFIRE)
                .setName(TextHelper.getTextByKey(getPlayer(), "profiler.gc"))
                .setLore(List.of(
                    TextHelper.getTextByKey(getPlayer(), "profiler.gc.name", name)
                    , TextHelper.getTextByKey(getPlayer(), "profiler.gc.average_gc_time", avgTime)
                    , TextHelper.getTextByKey(getPlayer(), "profiler.gc.total_gc_time", totalGcTime)
                    , TextHelper.getTextByKey(getPlayer(), "profiler.gc.average_frequency", avgFrequency)
                    , TextHelper.getTextByKey(getPlayer(), "profiler.gc.total_gc_count", totalGcCount)
                ))
                .build();
            elements.add(GuiElementIR.of(element));
        }

        return elements;
    }
}
