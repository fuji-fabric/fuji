package io.github.sakurawald.module.initializer.profiler;

import com.mojang.brigadier.context.CommandContext;
import io.github.sakurawald.core.annotation.Document;
import io.github.sakurawald.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.core.command.annotation.CommandNode;
import io.github.sakurawald.core.command.annotation.CommandSource;
import io.github.sakurawald.module.initializer.ModuleInitializer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.MutableText;
import org.jetbrains.annotations.NotNull;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryType;
import java.lang.management.MemoryUsage;
import java.util.List;
import java.util.concurrent.CompletableFuture;


public class ProfilerInitializer extends ModuleInitializer {

    private static @NotNull String formatBytes(long bytes) {
        if (bytes == -1) return "N/A";
        if (bytes < 1024) {
            return bytes + "B";
        } else if (bytes < 1024 * 1024) {
            return String.format("%.2fK", (double) bytes / 1024);
        } else if (bytes < 1024 * 1024 * 1024) {
            return String.format("%.2fM", (double) bytes / (1024 * 1024));
        } else {
            return String.format("%.2fG", (double) bytes / (1024 * 1024 * 1024));
        }
    }

    @CommandNode("profiler")
    @Document("Query the server health status.")
    private static int $profiler(@CommandSource CommandContext<ServerCommandSource> ctx) {
        ServerCommandSource source = ctx.getSource();
        CompletableFuture.runAsync(() -> {
            /* format */
            String os_name = ManagementFactory.getOperatingSystemMXBean().getName();
            String os_version = ManagementFactory.getOperatingSystemMXBean().getVersion();
            String os_arch = ManagementFactory.getOperatingSystemMXBean().getArch();

            String vmName = ManagementFactory.getRuntimeMXBean().getVmName();
            String vmVersion = ManagementFactory.getRuntimeMXBean().getVmVersion();

            /* gc */
            MutableText gcText = TextHelper.getTextByKey(source, "profiler.format.gc.head").copy();
            List<GarbageCollectorMXBean> gcMXBeans = ManagementFactory.getGarbageCollectorMXBeans();

            long uptime = ManagementFactory.getRuntimeMXBean().getUptime();

            int i = 0;
            for (GarbageCollectorMXBean gcMXBean : gcMXBeans) {
                String name = gcMXBean.getName();
                long totalGcTime = gcMXBean.getCollectionTime();
                long totalGcCount = gcMXBean.getCollectionCount();
                double avgFrequency = (double) uptime / totalGcCount / 1000;
                double avgTime = (double) totalGcTime / totalGcCount;

                gcText = gcText.append(TextHelper.getTextByKey(source, i == gcMXBeans.size() - 1 ? "profiler.format.gc.last" : "profiler.format.gc.no_last", name, avgFrequency, avgTime, totalGcCount, totalGcTime));
                i++;
            }

            /* mem */
            MutableText memText = TextHelper.getTextByKey(source, "profiler.format.mem.head").copy();
            List<MemoryPoolMXBean> memoryPoolMXBeans = ManagementFactory.getMemoryPoolMXBeans();
            i = 0;
            for (MemoryPoolMXBean memoryPoolMXBean : memoryPoolMXBeans) {
                String name = memoryPoolMXBean.getName();
                MemoryType type = memoryPoolMXBean.getType();
                MemoryUsage memoryUsage = memoryPoolMXBean.getUsage();
                String init = formatBytes(memoryUsage.getInit());
                String used = formatBytes(memoryUsage.getUsed());
                String committed = formatBytes(memoryUsage.getCommitted());
                String max = formatBytes(memoryUsage.getMax());
                memText = memText.append(TextHelper.getTextByKey(source, i == memoryPoolMXBeans.size() - 1 ? "profiler.format.mem.last" : "profiler.format.mem.no_last", name, type, init, used, committed, max));
                i++;
            }

            /* output */
            MutableText formatText = TextHelper.getTextByKey(source, "profiler.format"
                , os_name, os_version, os_arch
                , vmName, vmVersion).copy();
            source.sendMessage(
                formatText
                    .append(TextHelper.TEXT_NEWLINE).append(memText)
                    .append(TextHelper.TEXT_NEWLINE).append(gcText));
        });

        return CommandHelper.Return.SUCCESS;
    }
}
