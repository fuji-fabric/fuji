package mod.fuji.module.initializer.sit;

import mod.fuji.core.annotation.Unused;
import mod.fuji.core.auxiliary.minecraft.PlayerHelper;
import mod.fuji.core.document.annotation.Cite;
import mod.fuji.core.document.annotation.Document;
import mod.fuji.core.auxiliary.minecraft.CommandHelper;
import mod.fuji.core.auxiliary.minecraft.EntityHelper;
import mod.fuji.core.auxiliary.minecraft.TextHelper;
import mod.fuji.core.auxiliary.minecraft.WorldHelper;
import mod.fuji.core.command.annotation.CommandNode;
import mod.fuji.core.command.annotation.CommandSource;
import mod.fuji.core.command.annotation.CommandTarget;
import mod.fuji.core.config.handler.abst.BaseConfigurationHandler;
import mod.fuji.core.config.handler.impl.ObjectConfigurationHandler;
import mod.fuji.core.document.annotation.TestCase;
import mod.fuji.core.event.annotation.EventConsumer;
import mod.fuji.core.event.message.player.PlayerInteractBlockPreEvent;
import mod.fuji.core.event.message.server.lifecycle.ServerStoppingEvent;
import mod.fuji.module.initializer.ModuleInitializer;
import mod.fuji.module.initializer.sit.config.model.SitConfigModel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.SupportType;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.level.Level;
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

    private static final Vec3 CHAIR_ENTITY_OFFSET =
        #if MC_VER <= MC_1_20_1
            new Vec3d(0, -1.175, 0);
        #elif MC_VER > MC_1_20_1
            new Vec3(0, -1.375, 0);
        #endif

    private static final Set<Entity> SPAWNED_CHAIR_ENTITY_LIST = new HashSet<>();

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean canSitNow(ServerPlayer player) {
        return player.onGround()
                && !player.isPassenger()
                && !player.isSleeping()
                && !player.isSwimming()
                && !player.isSpectator();
    }

    @SuppressWarnings("deprecation")
    @Document(id = 1751827002061L, value = "Sit in current position.")
    @CommandNode("sit")
    private static int $sit(@CommandSource @CommandTarget ServerPlayer player) {
        /* Check if we can sit at player's position. */
        // NOTE: Use the stepping block pos, so that we can always get the proper height, even if the player is standing on top of a slab/stair block.
        BlockPos steppingBlockPos = player.getOnPos();
        ServerLevel serverWorld = PlayerHelper.getServerWorld(player);
        BlockState steppingBlockState = serverWorld.getBlockState(steppingBlockPos);
        if (!canSitNow(player)
            || steppingBlockState.isAir()
            || steppingBlockState.liquid()) {
            TextHelper.sendTextByKey(player, "sit.fail");
            return CommandHelper.Return.FAILURE;
        }

        /* Spawn the chair entity, and let the player ride it. */
        Vec3 lookTarget = EntityHelper.getPos(player).add(0.5, 0, 0.5);
        Entity chairEntity = spawnChairEntity(serverWorld, steppingBlockPos, lookTarget);
        SPAWNED_CHAIR_ENTITY_LIST.add(chairEntity);
        EntityHelper.rideEntity(player, chairEntity);

        return CommandHelper.Return.SUCCESS;
    }

    @SuppressWarnings({"Convert2MethodRef", "UnnecessaryLocalVariable"})
    private static double computeSensibleLengthY(VoxelShape voxelShape) {
        double averageLengthY = voxelShape.toAabbs().stream().mapToDouble(it -> {
                #if MC_VER <= MC_1_20_1
                    return it.getYLength();
                #elif MC_VER > MC_1_20_1
                    return it.getYsize();
                #endif
        }).average().orElse(0);
        return averageLengthY;
    }

    @SuppressWarnings("Convert2MethodRef")
    public static @NotNull Entity spawnChairEntity(@NotNull Level world, @NotNull BlockPos targetBlockPos, @Nullable Vec3 lookingTarget) {

        /* Compute the chair entity position. */
        Vec3 chairEntityPosition =
            WorldHelper.toBottomCenterPos(targetBlockPos)
            .add(0, 0.5, 0)
            .add(SitInitializer.CHAIR_ENTITY_OFFSET);
        BlockState targetBlockState = world.getBlockState(targetBlockPos);

        /* Compute the proper height using outline shape. */
        // NOTE: If the block under the chair entity is empty, then the player should not be falling.
        VoxelShape outlineShape = targetBlockState.getShape(world, targetBlockPos);
        double averageLengthY = computeSensibleLengthY(outlineShape);
        if (!Block.isFaceFull(outlineShape, Direction.UP)) {
            chairEntityPosition = chairEntityPosition.add(0, - (1 - averageLengthY), 0);
        }

        /* Make the chair entity using armor stand entity. */
        ArmorStand chairEntity = new ArmorStand(world, chairEntityPosition.x, chairEntityPosition.y, chairEntityPosition.z) {

            private boolean hasPassenger = false;
            private Vec3 dismountOffset = new Vec3(0, averageLengthY,0);

            @Override
            public void addPassenger(Entity passenger) {
                super.addPassenger(passenger);
                hasPassenger = true;
            }

            @Override
            public boolean isColliding(BlockPos blockPos, BlockState blockState) {
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

            private Vec3 getDismountPosition() {
                return getChairBlockPos()
                    .getCenter()
                    .add(dismountOffset);
            }

            @Override
            public Vec3 getDismountLocationForPassenger(LivingEntity livingEntity) {
                return getDismountPosition();
            }

            @Override
            protected void removePassenger(@NotNull Entity entity) {
                /* Call super to handle the default logic. */
                super.removePassenger(entity);

                /* If the chair block is broken, kick the player. */
                if (isChairBlockBroken()) {
                    Vec3 dismountPosition = getDismountPosition();
                    // NOTE: If the chair block is broken, the method `updatePassengerForDismount` will not be called. So we have to update the position manually.
                    EntityHelper.moveEntity(entity, dismountPosition.x, dismountPosition.y, dismountPosition.z, entity.getYRot(), entity.getXRot());
                    EntityHelper.killEntity(this);
                }
            }

            @Override
            public void tick() {
                /* Kill the chair entity, if the player left.  */
                if (hasPassenger && getPassengers().isEmpty()) {
                    EntityHelper.killEntity(this);
                }

                /* Kill the chair entity, if the bounding block is broken. */
                if (isChairBlockBroken()) {
                    EntityHelper.killEntity(this);
                }

                /* Sync the player's leg position. */
                Entity passenger = getFirstPassenger();
                if (passenger != null) {
                    this.setYRot(passenger.getYRot());
                    this.setXRot(passenger.getXRot());
                }

                /* Call super to handle default logic. */
                super.tick();
            }

        };

        /* Set the properties of the chair entity. */
        if (lookingTarget != null) {
            chairEntity.lookAt(EntityAnchorArgument.Anchor.EYES, lookingTarget.subtract(0, lookingTarget.y() * 2, 0));
        }
        chairEntity.setInvisible(true);
        chairEntity.setInvulnerable(true);
        chairEntity.setCustomName(Component.literal("FUJI-SIT"));
        chairEntity.setNoGravity(true);

        /* Spawn the chair entity. */
        world.addFreshEntity(chairEntity);
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
        ServerPlayer player = event.getPlayer();

        /* Verify. */
        var config = SitInitializer.config.model();
        if (!config.right_click_to_sit.enable) return;
        if (!config.right_click_to_sit.allow_sneaking_to_sit && player.isShiftKeyDown()) return;
        if (!SitInitializer.canSitNow(player)) return;
        if (config.right_click_to_sit.require_empty_hand_to_sit && !player.getMainHandItem().isEmpty()) return;

        // Verify surrounding blocks.
        Level world = event.getWorld();
        BlockHitResult blockHitResult = event.getBlockHitResult();
        BlockPos hitBlockPos = blockHitResult.getBlockPos();
        BlockState hitBlockState = world.getBlockState(hitBlockPos);
        Block hitBlock = hitBlockState.getBlock();
        if (config.right_click_to_sit.require_no_opaque_block_above_to_sit && world.getBlockState(hitBlockPos.offset(0, 1, 0)).canOcclude()) return;

        // Only allow to right-click to sit on stair block or slab block.
        if (!(hitBlock instanceof StairBlock) && !(hitBlock instanceof SlabBlock)) return;

        // The face of chair must be up.
        if (hitBlockState.isFaceSturdy(world, hitBlockPos, Direction.UP, SupportType.RIGID)) return;

        // Verify max distance to sit.
        final double maxDistanceToSit = config.right_click_to_sit.max_distance_to_sit;
        double givenDist = hitBlockPos.distSqr(player.blockPosition());
        if (maxDistanceToSit > 0 && givenDist > maxDistanceToSit * maxDistanceToSit) return;

        /* Spawn the chair entity and ride it. */
        Vec3 lookingTarget = EntityHelper.getPos(player).add(0.5, 0, 0.5);
        Entity chairEntity = SitInitializer.spawnChairEntity(world, hitBlockPos, lookingTarget);

        // Dismount the player if there is another vehicle.
        Entity currentVehicleEntity = player.getVehicle();
        if (currentVehicleEntity != null) {
            PlayerHelper.dismountRidingEntity(player);
        }

        // Ride the chair entity.
        EntityHelper.rideEntity(player, chairEntity);
        event.getCallbackInfoReturnable().setReturnValue(InteractionResult.SUCCESS);
    }

}
