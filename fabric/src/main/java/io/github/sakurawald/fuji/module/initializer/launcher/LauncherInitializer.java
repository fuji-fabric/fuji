package io.github.sakurawald.fuji.module.initializer.launcher;

import io.github.sakurawald.fuji.core.auxiliary.LogUtil;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.EntityHelper;
import io.github.sakurawald.fuji.core.command.annotation.CommandNode;
import io.github.sakurawald.fuji.core.command.annotation.CommandRequirement;
import io.github.sakurawald.fuji.core.command.annotation.CommandSource;
import io.github.sakurawald.fuji.core.command.argument.wrapper.impl.EntityCollection;
import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.core.event.impl.ServerTickEvents;
import io.github.sakurawald.fuji.core.manager.Managers;
import io.github.sakurawald.fuji.core.manager.impl.task.structure.GameTask;
import io.github.sakurawald.fuji.module.initializer.ModuleInitializer;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.entity.Entity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;


@Document(id = 1756347408511L, value = """
    Launch an `entity` with a `velocity`.
    """)
public class LauncherInitializer extends ModuleInitializer {

//    // NOTE: The base velocity for walking and flying is different.
//    private static final double BLOCKS_PER_TICK = 2.2010;
//    // In vanilla Minecraft, the max distance an entity can go through in 20 ticks is 82.184 blocks.
//    // For a still player, the gravity acceleration is (0.0, -0.0784000015258789, 0.0). If the player is flying, then it's (0, 0, 0)
//    // The max distance an entity can go through in 1 tick is 9.792
//    // 8.084
//
//    @CommandNode("launch")
//    @CommandRequirement(level = 4)
//    private static int $launch(@CommandSource ServerCommandSource source, EntityCollection target, double x, double y, double z, int ticks) {
//        List<GameTask> createdTasks = new ArrayList<>();
//        target
//            .getValue()
//            .forEach(entity -> {
//                GameTask gameTask = new GameTask(ticks, () -> {
//                    LogUtil.warn("onTick() -> velocity = {}", entity.getVelocity());
//                    EntityHelper.setVelocity(entity, x, y, z);
//                }, () -> {
//                    LogUtil.warn("onStart() -> velocity = {}", entity.getVelocity());
//                }, () -> {
//                    LogUtil.warn("onEnd() -> velocity = {}", entity.getVelocity());
//                });
//                createdTasks.add(gameTask);
//            });
//
//        Managers.getGameTaskManager().submitTasks(createdTasks);
//        return CommandHelper.Return.SUCCESS;
//    }


}
