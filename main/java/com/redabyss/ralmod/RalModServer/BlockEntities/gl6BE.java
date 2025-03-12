package com.redabyss.ralmod.RalModServer.BlockEntities;

import com.redabyss.ralmod.RalModRegist.BlockEntities;
import com.redabyss.ralmod.RalModScript.Rotation;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class gl6BE extends BlockEntity{
    private int redstonePower=0;
    private float YawAngle;
    private float PitchAngle;
    private float TimePass=0.0f; //计时器
    public gl6BE(BlockPos p_155229_, BlockState p_155230_) {
        super(BlockEntities.GL6_BE.get(), p_155229_, p_155230_);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, gl6BE be) {
        if (!level.isClientSide) {
            int newPower = level.getBestNeighborSignal(pos);
            if (newPower != be.redstonePower) {
                be.redstonePower = newPower;
                }
            be.Rot(be.redstonePower*12f, be.redstonePower*12f);
            be.setChanged(); // 标记数据需要保存
            level.sendBlockUpdated(pos, state, state, Block.UPDATE_ALL);
        }
    }
    public int getRedstonePower() {return redstonePower;}
    public float getYawAngle() {return YawAngle;}
    public float getPitchAngle() { return PitchAngle;}
    //旋转实现
    public void Rot(float goalYawAngle,float goalPitchAngle) {
            YawAngle += Rotation.NowAngleToGoalAngle(YawAngle,goalYawAngle);
            PitchAngle += Rotation.NowAngleToGoalAngle(PitchAngle,goalPitchAngle);
            this.setChanged();
    }
    //载入NBT
    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("RedstonePower", redstonePower);
        tag.putFloat("YawAngle", YawAngle);
        tag.putFloat("PitchAngle", PitchAngle);
        tag.putFloat("TimePass", TimePass);
    }
    //加载NBT
    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        redstonePower = tag.getInt("RedstonePower");
        YawAngle = tag.getFloat("YawAngle");
        PitchAngle = tag.getFloat("PitchAngle");
        TimePass = tag.getFloat("TimePass");
    }
    // 同步数据到客户端 打包
    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        tag.putInt("RedstonePower", redstonePower);
        tag.putFloat("YawAngle", YawAngle);
        tag.putFloat("PitchAngle", PitchAngle);
        tag.putFloat("TimePass", TimePass);
        return tag;
    }
    // 拆包
    @Override
    public void handleUpdateTag(CompoundTag tag) {
        super.handleUpdateTag(tag);
        redstonePower = tag.getInt("RedstonePower");
        YawAngle = tag.getFloat("YawAngle");
        PitchAngle = tag.getFloat("PitchAngle");
        TimePass = tag.getFloat("TimePass");
    }
    //服务端客户端强制同步(大概是吧)
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
