package io.github.sakurawald.fuji.module.initializer.sit;

import io.github.sakurawald.fuji.core.annotation.Unused;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.PlayerHelper;
import io.github.sakurawald.fuji.core.document.annotation.Cite;
import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.EntityHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.WorldHelper;
import io.github.sakurawald.fuji.core.command.annotation.CommandNode;
import io.github.sakurawald.fuji.core.command.annotation.CommandSource;
import io.github.sakurawald.fuji.core.command.annotation.CommandTarget;
import io.github.sakurawald.fuji.core.config.handler.abst.BaseConfigurationHandler;
import io.github.sakurawald.fuji.core.config.handler.impl.ObjectConfigurationHandler;
import io.github.sakurawald.fuji.core.document.annotation.TestCase;
import io.github.sakurawald.fuji.core.event.annotation.EventConsumer;
import io.github.sakurawald.fuji.core.event.message.player.PlayerInteractBlockPreEvent;
import io.github.sakurawald.fuji.core.event.message.server.lifecycle.ServerStoppingEvent;
import io.github.sakurawald.fuji.module.initializer.ModuleInitializer;
import io.github.sakurawald.fuji.module.initializer.sit.config.model.SitConfigModel;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SideShapeType;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.StairsBlock;
import net.minecraft.command.argument.EntityAnchorArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;

@Cite("https://github.com/BradBot1/FabricSit")
@Document(id = 1751826999379L, value = """
    Provides a facility to sit on blocks.
    """)
@TestCase(action = "Issue `/sit` command while stepping on the `bed block`.", targets = "The raycast height should be proper.")
public class SitInitializer extends ModuleInitializer {

    public static final BaseConfigurationHandler<SitConfigModel> config = ObjectConfigurationHandler.ofModule(BaseConfigurationHandler.CONFIG_JSON_LITERAL, SitConfigModel.class);

    private static final Vec3d CHAIR_ENTITY_OFFSET =
        #if MC_VER <= MC_1_20_1
            new Vec3d(0, -1.175, 0);
        #elif MC_VER > MC_1_20_1
            new Vec3d(0, -1.375, 0);
        #endif

    private static final Set<Entity> SPAWNED_CHAIR_ENTITY_LIST = new HashSet<>();

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean canSitNow(ServerPlayerEntity player) {
        return player.isOnGround()
                && !player.hasVehicle()
                && !player.isSleeping()
                && !player.isSwimming()
                && !player.isSpectator();
    }

    @SuppressWarnings("deprecation")
    @Document(id = 1751827002061L, value = "Sit in current position.")
    @CommandNode("sit")
    private static int $sit(@CommandSource @CommandTarget ServerPlayerEntity player) {
        /* Check if we can sit at player's position. */
        // NOTE: Use the stepping block pos, so that we can always get the proper height, even if the player is standing on top of a slab/stair block.
        BlockPos steppingBlockPos = player.getSteppingPos();
        BlockState steppingBlockState = player.getWorld().getBlockState(steppingBlockPos);
        if (!canSitNow(player)
            || steppingBlockState.isAir()
            || steppingBlockState.isLiquid()) {
            TextHelper.sendTextByKey(player, "sit.fail");
            return CommandHelper.Return.FAILURE;
        }

        /* Spawn the chair entity, and let the player ride it. */
        Vec3d lookTarget = player.getPos().add(0.5, 0, 0.5);
        Entity chairEntity = spawnChairEntity(player.getWorld(), steppingBlockPos, lookTarget);
        SPAWNED_CHAIR_ENTITY_LIST.add(chairEntity);
        player.startRiding(chairEntity, true);

        return CommandHelper.Return.SUCCESS;
    }

    @SuppressWarnings({"Convert2MethodRef", "UnnecessaryLocalVariable"})
    private static double computeSensibleLengthY(VoxelShape voxelShape) {
        double averageLengthY = voxelShape.getBoundingBoxes().stream().mapToDouble(it -> {
                #if MC_VER <= MC_1_20_1
                    return it.getYLength();
                #elif MC_VER > MC_1_20_1
                    return it.getLengthY();
                #endif
        }).average().orElse(0);
        return averageLengthY;
    }

    @SuppressWarnings("Convert2MethodRef")
    public static @NotNull Entity spawnChairEntity(@NotNull World world, @NotNull BlockPos targetBlockPos, @Nullable Vec3d lookingTarget) {

        /* Compute the chair entity position. */
        Vec3d chairEntityPosition =
            WorldHelper.toBottomCenterPos(targetBlockPos)
            .add(0, 0.5, 0)
            .add(SitInitializer.CHAIR_ENTITY_OFFSET);
        BlockState targetBlockState = world.getBlockState(targetBlockPos);

        /* Compute the proper height using outline shape. */
        // NOTE: If the block under the chair entity is empty, then the player should not be falling.
        VoxelShape outlineShape = targetBlockState.getOutlineShape(world, targetBlockPos);
        double averageLengthY = computeSensibleLengthY(outlineShape);
        if (!Block.isFaceFullSquare(outlineShape, Direction.UP)) {
            chairEntityPosition = chairEntityPosition.add(0, - (1 - averageLengthY), 0);
        }

        /* Make the chair entity using armor stand entity. */
        ArmorStandEntity chairEntity = new ArmorStandEntity(world, chairEntityPosition.x, chairEntityPosition.y, chairEntityPosition.z) {

            private boolean hasPassenger = false;
            private Vec3d dismountOffset = new Vec3d(0, averageLengthY,0);

            @Override
            public void addPassenger(Entity passenger) {
                super.addPassenger(passenger);
                hasPassenger = true;
            }

            @Override
            public boolean canMoveVoluntarily() {
                return false;
            }

            @Override
            public boolean collidesWithStateAtPos(BlockPos blockPos, BlockState blockState) {
                return false;
            }

            private BlockPos getChairBlockPos() {
                return targetBlockPos;
            }

            private boolean isChairBlockBroken() {
                /* Kill the chair entity, if the binding block is broken. */
                return EntityHelper.getServerWorld(this)
                        .getBlockState(getChairBlockPos())
                        .isAir();
            }

            private Vec3d getDismountPosition() {
                return getChairBlockPos()
                    .toCenterPos()
                    .add(dismountOffset);
            }

            @Override
            public Vec3d updatePassengerForDismount(LivingEntity livingEntity) {
                return getDismountPosition();
            }

            @Override
            protected void removePassenger(Entity entity) {
                /* Call super to handle the default logic. */
                super.removePassenger(entity);

                /* If the chair block is broken, kick the player. */
                if (isChairBlockBroken()) {
                    Vec3d dismountPosition = getDismountPosition();
                    // NOTE: If the chair block is broken, the method `updatePassengerForDismount` will not be called. So we have to update the position manually.
                    entity.refreshPositionAndAngles(dismountPosition.x, dismountPosition.y, dismountPosition.z, entity.getYaw(), entity.getPitch());
                    EntityHelper.killEntity(this);
                }
            }

            @Override
            public void tick() {
                /* Kill the chair entity, if the player left.  */
                if (hasPassenger && getPassengerList().isEmpty()) {
                    EntityHelper.killEntity(this);
                }

                /* Kill the chair entity, if the bounding block is broken. */
                if (isChairBlockBroken()) {
                    EntityHelper.killEntity(this);
                }

                /* Sync the player's leg position. */
                Entity passenger = getFirstPassenger();
                if (passenger != null) {
                    this.setYaw(passenger.getYaw());
                    this.setPitch(passenger.getPitch());
                }

                /* Call super to handle default logic. */
                super.tick();
            }

        };

        /* Set the properties of the chair entity. */
        if (lookingTarget != null) {
            chairEntity.lookAt(EntityAnchorArgumentType.EntityAnchor.EYES, lookingTarget.subtract(0, lookingTarget.getY() * 2, 0));
        }
        chairEntity.setInvisible(true);
        chairEntity.setInvulnerable(true);
        chairEntity.setCustomName(Text.literal("FUJI-SIT"));
        chairEntity.setNoGravity(true);

        /* Spawn the chair entity. */
        world.spawnEntity(chairEntity);
        return chairEntity;
    }

    @EventConsumer
    private static void killSpawnedSitEntities(@Unused ServerStoppingEvent event) {
        SPAWNED_CHAIR_ENTITY_LIST.forEach(entity -> {
            if (entity.isAlive()) {
                EntityHelper.killEntity(entity);
            }
        });
    }

    @EventConsumer
    private static void consumePlayerInteractBlockPreEvent(PlayerInteractBlockPreEvent event) {
        if (event.getCallbackInfoReturnable().isCancelled()) return;
        ServerPlayerEntity player = event.getPlayer();

        /* Verify. */
        var config = SitInitializer.config.model();
        if (!config.right_click_to_sit.enable) return;
        if (!config.right_click_to_sit.allow_sneaking_to_sit && player.isSneaking()) return;
        if (!SitInitializer.canSitNow(player)) return;
        if (config.right_click_to_sit.require_empty_hand_to_sit && !player.getMainHandStack().isEmpty()) return;

        // Verify surrounding blocks.
        World world = event.getWorld();
        BlockHitResult blockHitResult = event.getBlockHitResult();
        BlockPos hitBlockPos = blockHitResult.getBlockPos();
        BlockState hitBlockState = world.getBlockState(hitBlockPos);
        Block hitBlock = hitBlockState.getBlock();
        if (config.right_click_to_sit.require_no_opaque_block_above_to_sit && world.getBlockState(hitBlockPos.add(0, 1, 0)).isOpaque()) return;

        // Only allow to right-click to sit on stair block or slab block.
        if (!(hitBlock instanceof StairsBlock) && !(hitBlock instanceof SlabBlock)) return;

        // The face of chair must be up.
        if (hitBlockState.isSideSolid(world, hitBlockPos, Direction.UP, SideShapeType.RIGID)) return;

        // Verify max distance to sit.
        final double maxDistanceToSit = config.right_click_to_sit.max_distance_to_sit;
        double givenDist = hitBlockPos.getSquaredDistance(player.getBlockPos());
        if (maxDistanceToSit > 0 && givenDist > maxDistanceToSit * maxDistanceToSit) return;

        /* Spawn the chair entity and ride it. */
        Vec3d lookingTarget = player.getPos().add(0.5, 0, 0.5);
        Entity chairEntity = SitInitializer.spawnChairEntity(world, hitBlockPos, lookingTarget);

        // Dismount the player if there is another vehicle.
        Entity currentVehicleEntity = player.getVehicle();
        if (currentVehicleEntity != null) {
            PlayerHelper.dismountRidingEntity(player);
        }

        // Ride the chair entity.
        player.startRiding(chairEntity, true);
        event.getCallbackInfoReturnable().setReturnValue(ActionResult.SUCCESS);
    }

}
