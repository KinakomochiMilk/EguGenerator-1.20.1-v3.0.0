
package com.kinakomochimilk.generatooooor;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EguGeneratorBlockEntity extends BlockEntity
        implements ILongEnergyStorage {

    // 最大蓄電容量：10 TFE
    public static final long MAX_ENERGY = 10_000_000_000_000L;

    public static final long COMPRESSED_MAGMA_GENERATION = 50_000_000_000L;
    public static final long DOUBLE_COMPRESSED_MAGMA_GENERATION = 100_000_000_000L;
    public static final long TRIPLE_COMPRESSED_MAGMA_GENERATION = 250_000_000_000L;

    // 最大送電速度：1,000 TFE/t
    public static final long MAX_TRANSFER_PER_TICK =
            1_000_000_000_000_000L;

    private long energy = 0L;

    private final IEnergyStorage energyStorage = new IEnergyStorage() {

        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            return 0;
        }

        @Override
        public int extractEnergy(int maxExtract, boolean simulate) {
            if (maxExtract <= 0 || energy <= 0L) {
                return 0;
            }

            int extracted = (int) Math.min((long) maxExtract, energy);

            if (!simulate && extracted > 0) {
                energy -= extracted;
                setChanged();
                syncEnergy();
            }

            return extracted;
        }

        @Override
        public int getEnergyStored() {
            return (int) Math.min(energy, Integer.MAX_VALUE);
        }

        @Override
        public int getMaxEnergyStored() {
            return Integer.MAX_VALUE;
        }

        @Override
        public boolean canExtract() {
            return true;
        }

        @Override
        public boolean canReceive() {
            return false;
        }
    };

    private final LazyOptional<IEnergyStorage> energyCapability =
            LazyOptional.of(() -> energyStorage);

    public EguGeneratorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.EGU_GENERATOR.get(), pos, state);
    }

    public long getEnergy() {
        return energy;
    }

    public long getMaxEnergy() {
        return MAX_ENERGY;
    }

    public long getFreeCapacity() {
        return MAX_ENERGY - energy;
    }

    /**
     * 周囲6方向にある燃料ブロックから発電量を計算する。
     */
    public long getGenerationPerTick() {
        if (level == null) {
            return 0L;
        }

        long generation = 0L;

        for (Direction direction : Direction.values()) {
            BlockPos adjacentPos = worldPosition.relative(direction);
            BlockState adjacentState = level.getBlockState(adjacentPos);

            if (adjacentState.is(ModBlocks.COMPRESSED_MAGMA_BLOCK.get())) {
                generation += COMPRESSED_MAGMA_GENERATION;
            } else if (adjacentState.is(
                    ModBlocks.DOUBLE_COMPRESSED_MAGMA_BLOCK.get())) {
                generation += DOUBLE_COMPRESSED_MAGMA_GENERATION;
            } else if (adjacentState.is(
                    ModBlocks.TRIPLE_COMPRESSED_MAGMA_BLOCK.get())) {
                generation += TRIPLE_COMPRESSED_MAGMA_GENERATION;
            }
        }

        return generation;
    }

    /**
     * サーバー側の発電処理。
     */
    public void generateEnergy() {
        if (level == null || level.isClientSide) {
            return;
        }

        long generation = getGenerationPerTick();
        long generated = Math.min(generation, getFreeCapacity());

        if (generated > 0L) {
            energy += generated;
            setChanged();
            syncEnergy();

            // 40 tickごとにかまどの燃焼音を再生
            if (level.getGameTime() % 40L == 0L) {
                level.playSound(
                        null,
                        worldPosition,
                        SoundEvents.FURNACE_FIRE_CRACKLE,
                        SoundSource.BLOCKS,
                        0.5F,
                        1.0F
                );
            }
        }

        transferEnergy();
    }

    /**
     * 隣接ブロックへエネルギーを送る。
     * 大容量API対応ブロックを優先し、通常のForge Energyにも対応する。
     */
    private void transferEnergy() {
        if (level == null || level.isClientSide || energy <= 0L) {
            return;
        }

        // 出力面はFACINGと同じ方向
        Direction facing = getBlockState().getValue(EguGeneratorBlock.FACING);
        Direction outputDirection = facing;

        BlockPos targetPos = worldPosition.relative(outputDirection);
        BlockEntity target = level.getBlockEntity(targetPos);

        if (target == null) {
            return;
        }

        long offered = Math.min(energy, MAX_TRANSFER_PER_TICK);

        if (offered <= 0L) {
            return;
        }

        // Egu Generator独自のlong型エネルギーAPI
        if (target instanceof ILongEnergyStorage longStorage) {
            if (!longStorage.canReceiveLongEnergy()) {
                return;
            }

            long accepted = longStorage.receiveLongEnergy(offered, true);
            accepted = Math.max(0L, Math.min(accepted, offered));

            if (accepted <= 0L) {
                return;
            }

            long extracted = extractLongEnergy(accepted, false);

            if (extracted <= 0L) {
                return;
            }

            long received = longStorage.receiveLongEnergy(extracted, false);
            received = Math.max(0L, Math.min(received, extracted));

            if (received < extracted) {
                energy = Math.min(
                        MAX_ENERGY,
                        energy + extracted - received
                );
            }

            setChanged();
            syncEnergy();
            return;
        }

        // 通常のForge Energy APIとの互換性
        LazyOptional<IEnergyStorage> targetStorage =
                target.getCapability(
                        ForgeCapabilities.ENERGY,
                        outputDirection.getOpposite()
                );

        targetStorage.ifPresent(storage -> {
            if (!storage.canReceive()) {
                return;
            }

            int offeredInt = (int) Math.min(
                    offered,
                    Integer.MAX_VALUE
            );

            int accepted = storage.receiveEnergy(offeredInt, true);
            accepted = Math.max(0, Math.min(accepted, offeredInt));

            if (accepted <= 0) {
                return;
            }

            long extracted = extractLongEnergy(accepted, false);

            if (extracted <= 0L) {
                return;
            }

            int received = storage.receiveEnergy((int) extracted, false);
            received = Math.max(0, Math.min(received, (int) extracted));

            if (received < extracted) {
                energy = Math.min(
                        MAX_ENERGY,
                        energy + extracted - received
                );
            }

            setChanged();
            syncEnergy();
        });
    }

    /**
     * long型エネルギーを受け取る。
     */
    @Override
    public long receiveLongEnergy(long maxReceive, boolean simulate) {
        if (maxReceive <= 0L) {
            return 0L;
        }

        long accepted = Math.min(maxReceive, getFreeCapacity());

        if (!simulate && accepted > 0L) {
            energy += accepted;
            setChanged();
            syncEnergy();
        }

        return accepted;
    }

    /**
     * long型エネルギーを取り出す。
     */
    @Override
    public long extractLongEnergy(long maxExtract, boolean simulate) {
        if (maxExtract <= 0L || energy <= 0L) {
            return 0L;
        }

        long extracted = Math.min(maxExtract, energy);

        if (!simulate && extracted > 0L) {
            energy -= extracted;
            setChanged();
            syncEnergy();
        }

        return extracted;
    }

    @Override
    public long getLongEnergyStored() {
        return energy;
    }

    @Override
    public long getMaxLongEnergyStored() {
        return MAX_ENERGY;
    }

    @Override
    public boolean canReceiveLongEnergy() {
        return false;
    }

    @Override
    public boolean canExtractLongEnergy() {
        return true;
    }

    /**
     * 通常のForge Energy APIは出力面だけに公開する。
     */
    @Override
    public <T> @NotNull LazyOptional<T> getCapability(
            @NotNull Capability<T> capability,
            @Nullable Direction side
    ) {
        if (capability == ForgeCapabilities.ENERGY) {
            Direction facing =
                    getBlockState().getValue(EguGeneratorBlock.FACING);
            Direction outputDirection = facing;

            if (side == outputDirection) {
                return energyCapability.cast();
            }
        }

        return super.getCapability(capability, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        energyCapability.invalidate();
    }

    private void syncEnergy() {
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(
                    worldPosition,
                    getBlockState(),
                    getBlockState(),
                    3
            );
        }
    }

    @Override
    public CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putLong("Energy", energy);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);

        energy = Math.max(
                0L,
                Math.min(MAX_ENERGY, tag.getLong("Energy"))
        );
    }
}
