package com.redabysslucia.dragonrise_reforge.entities.vehicle;

import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import com.atsuishio.superbwarfare.tools.OBB;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4d;

/** Independent, server-authoritative cabin doors with ordinary body OBBs. */
public abstract class SlidingDoorHelicopterEntity extends VehicleEntity {
    public static final int DOOR_ANIMATION_TICKS = 15;

    private static final EntityDataAccessor<Byte> LEFT_DOOR_STATE =
            SynchedEntityData.defineId(SlidingDoorHelicopterEntity.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Byte> RIGHT_DOOR_STATE =
            SynchedEntityData.defineId(SlidingDoorHelicopterEntity.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Long> LEFT_DOOR_STARTED =
            SynchedEntityData.defineId(SlidingDoorHelicopterEntity.class, EntityDataSerializers.LONG);
    private static final EntityDataAccessor<Long> RIGHT_DOOR_STARTED =
            SynchedEntityData.defineId(SlidingDoorHelicopterEntity.class, EntityDataSerializers.LONG);

    private final String animationPrefix;

    public enum DoorSide {
        LEFT("DoorLeft", "bone5", new Vec3(-2.0 / 16.0, 0, -36.0 / 16.0)),
        RIGHT("DoorRight", "bone6", new Vec3(0, 0, -37.0 / 16.0));

        public final String transform;
        public final String bone;
        public final Vec3 openOffset;

        DoorSide(String transform, String bone, Vec3 openOffset) {
            this.transform = transform;
            this.bone = bone;
            this.openOffset = openOffset;
        }
    }

    public enum DoorState {
        CLOSED, OPENING, OPEN, CLOSING;

        public boolean isMoving() {
            return this == OPENING || this == CLOSING;
        }

        public boolean targetsOpen() {
            return this == OPENING || this == OPEN;
        }

        public DoorState afterTicks(long elapsedTicks) {
            return isMoving() && elapsedTicks >= DOOR_ANIMATION_TICKS
                    ? (targetsOpen() ? OPEN : CLOSED) : this;
        }

        public DoorState toggleAfterTicks(long elapsedTicks) {
            DoorState settled = afterTicks(elapsedTicks);
            if (settled.isMoving()) return settled;
            return settled == CLOSED ? OPENING : CLOSING;
        }
    }

    protected SlidingDoorHelicopterEntity(EntityType<?> type, Level level, String animationPrefix) {
        super(type, level);
        this.animationPrefix = animationPrefix;
        // Native OBB update, damage and collision paths remain in VehicleEntity.
        for (DoorSide side : DoorSide.values()) {
            getPositionTransform().put(side.transform, partialTicks -> getDoorTransform(side, partialTicks));
        }
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(LEFT_DOOR_STATE, (byte) DoorState.CLOSED.ordinal());
        this.entityData.define(RIGHT_DOOR_STATE, (byte) DoorState.CLOSED.ordinal());
        this.entityData.define(LEFT_DOOR_STARTED, 0L);
        this.entityData.define(RIGHT_DOOR_STARTED, 0L);
    }

    private static EntityDataAccessor<Byte> stateKey(DoorSide side) {
        return side == DoorSide.LEFT ? LEFT_DOOR_STATE : RIGHT_DOOR_STATE;
    }

    private static EntityDataAccessor<Long> startedKey(DoorSide side) {
        return side == DoorSide.LEFT ? LEFT_DOOR_STARTED : RIGHT_DOOR_STARTED;
    }

    public DoorState getDoorState(DoorSide side) {
        return DoorState.values()[Mth.clamp(this.entityData.get(stateKey(side)), 0, 3)];
    }

    public String getDoorAnimation(DoorSide side) {
        return animationPrefix + "." + (getDoorState(side).targetsOpen() ? "open" : "close")
                + (side == DoorSide.LEFT ? "left" : "right") + "door.animation";
    }

    public float getDoorAnimationSeconds(DoorSide side, float partialTicks) {
        if (!getDoorState(side).isMoving()) return DOOR_ANIMATION_TICKS / 20f;
        double elapsed = this.level().getGameTime() - this.entityData.get(startedKey(side)) + partialTicks;
        return (float) Mth.clamp(elapsed, 0, DOOR_ANIMATION_TICKS) / 20f;
    }

    private Matrix4d getDoorTransform(DoorSide side, float partialTicks) {
        Matrix4d transform = getVehicleTransform(partialTicks);
        // Snap the physical box to the target pose as soon as opening/closing starts.
        if (getDoorState(side).targetsOpen()) {
            Vec3 offset = side.openOffset;
            transform.translate(offset.x, offset.y, offset.z);
        }
        return transform;
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        if (isWreck() || getLocked()) return super.interact(player, hand);

        updateOBB();
        // Same nearest-OBB ray test used by superb's Type 63; BODY boxes are supported too.
        OBB looking = OBB.getLookingObb(player, player.getEntityReach());
        if (looking != null) {
            for (var info : getObb()) {
                if (info.getOBB() != looking) continue;
                for (DoorSide side : DoorSide.values()) {
                    if (!side.transform.equals(info.getTransform())) continue;
                    if (!this.level().isClientSide && hand == InteractionHand.MAIN_HAND) {
                        toggleDoor(side);
                    }
                    // Consume the interaction even while moving, so it cannot mount the player.
                    return InteractionResult.sidedSuccess(this.level().isClientSide);
                }
            }
        }
        return super.interact(player, hand);
    }

    private void toggleDoor(DoorSide side) {
        DoorState state = getDoorState(side);
        DoorState next = state.toggleAfterTicks(this.level().getGameTime() - this.entityData.get(startedKey(side)));
        if (next == state) return;
        this.entityData.set(startedKey(side), this.level().getGameTime());
        this.entityData.set(stateKey(side), (byte) next.ordinal());
    }

    private void finishDoorMovement(DoorSide side) {
        DoorState state = getDoorState(side);
        DoorState next = state.afterTicks(this.level().getGameTime() - this.entityData.get(startedKey(side)));
        if (next != state) this.entityData.set(stateKey(side), (byte) next.ordinal());
    }

    @Override
    public void baseTick() {
        super.baseTick();
        if (!this.level().isClientSide) {
            finishDoorMovement(DoorSide.LEFT);
            finishDoorMovement(DoorSide.RIGHT);
        }
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);
        if ((key == LEFT_DOOR_STATE || key == RIGHT_DOOR_STATE) && isInitialized()) {
            updateOBB();
            refreshBoundingBoxFromOBBs();
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("LeftDoorOpen", getDoorState(DoorSide.LEFT).targetsOpen());
        tag.putBoolean("RightDoorOpen", getDoorState(DoorSide.RIGHT).targetsOpen());
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        // Restore completed poses; never replay an old transition when loading a saved vehicle.
        this.entityData.set(LEFT_DOOR_STATE, (byte) (tag.getBoolean("LeftDoorOpen")
                ? DoorState.OPEN.ordinal() : DoorState.CLOSED.ordinal()));
        this.entityData.set(RIGHT_DOOR_STATE, (byte) (tag.getBoolean("RightDoorOpen")
                ? DoorState.OPEN.ordinal() : DoorState.CLOSED.ordinal()));
    }
}
