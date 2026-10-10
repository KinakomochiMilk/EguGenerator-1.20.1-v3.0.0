
package com.kinakomochimilk.generatooooor;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EguCableBlockEntity extends BlockEntity
        implements ILongEnergyStorage {

    // 内部蓄電容量：10 TFE
    public static final long MAX_ENERGY = 10_000_000_000_000L;

    // 最大入出力：1,000 TFE/t
    public static final long MAX_TRANSFER_PER_TICK =
            1_000_000_000_000_000L;

    private long energy = 0L;

    private final IEnergyStorage energyStorage = new IEnergyStorage() {
        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            if (maxReceive <= 0) {
                return 0;
            }

            long accepted = receiveLongEnergy(
                    Math.min((long) maxReceive, MAX_TRANSFER_PER_TICK),
                    simulate
            );

            return (int) Math.min(accepted, Integer.MAX_VALUE);
        }

        @Override
        public int extractEnergy(int maxExtract, boolean simulate) {
            if (maxExtract <= 0) {
                return 0;
            }

            long extracted = extractLongEnergy(
                    Math.min((long) maxExtract, MAX_TRANSFER_PER_TICK),
                    simulate
            );

            return (int) Math.min(extracted, Integer.MAX_VALUE);
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
            return true;
        }
    };

    private final LazyOptional<IEnergyStorage> energyCapability =
            LazyOptional.of(() -> energyStorage);

    public EguCableBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.EGU_CABLE.get(), pos, state);
    }

    public static void serverTick(
            net.minecraft.world.level.Level level,
            BlockPos pos,
            BlockState state,
            EguCableBlockEntity cable
    ) {
        cable.serverTick();
    }

    public void serverTick() {
        if (level == null || level.isClientSide || energy <= 0L) {
            return;
        }

        long remainingTransfer = Math.min(
                energy,
                MAX_TRANSFER_PER_TICK
        );

        for (Direction direction : Direction.values()) {
            if (remainingTransfer <= 0L) {
                break;
            }

            BlockPos targetPos = worldPosition.relative(direction);
            BlockEntity target = level.getBlockEntity(targetPos);

            if (target == null || target == this) {
                continue;
            }

            // ケーブル同士の送電は、座標順を統一して往復を抑える。
            if (target instanceof EguCableBlockEntity) {
                if (comparePositions(worldPosition, targetPos) >= 0) {
                    continue;
                }
            }

            long sent = transferTo(target, direction, remainingTransfer);

            if (sent > 0L) {
                remainingTransfer -= sent;
            }
        }
    }

    private int comparePositions(BlockPos a, BlockPos b) {
        int result = Integer.compare(a.getX(), b.getX());

        if (result == 0) {
            result = Integer.compare(a.getY(), b.getY());
        }

        if (result == 0) {
            result = Integer.compare(a.getZ(), b.getZ());
        }

        return result;
    }

    private long transferTo(
            BlockEntity target,
            Direction direction,
            long limit
    ) {
        if (target instanceof ILongEnergyStorage longStorage
                && longStorage.canReceiveLongEnergy()) {

            long offered = Math.min(
                    limit,
                    Math.min(energy, MAX_TRANSFER_PER_TICK)
            );

            long simulated = longStorage.receiveLongEnergy(offered, true);
            long amount = Math.max(0L, Math.min(simulated, offered));

            if (amount <= 0L) {
                return 0L;
            }

            long extracted = extractLongEnergy(amount, false);

            if (extracted <= 0L) {
                return 0L;
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

            return received;
        }

        LazyOptional<IEnergyStorage> targetStorage =
                target.getCapability(
                        ForgeCapabilities.ENERGY,
                        direction.getOpposite()
                );

        if (!targetStorage.isPresent()) {
            return 0L;
        }

        final long[] sent = {0L};

        targetStorage.ifPresent(storage -> {
            if (!storage.canReceive()) {
                return;
            }

            int offered = (int) Math.min(
                    Math.min(limit, energy),
                    Integer.MAX_VALUE
            );

            if (offered <= 0) {
                return;
            }

            int simulated = storage.receiveEnergy(offered, true);
            int amount = Math.max(0, Math.min(simulated, offered));

            if (amount <= 0) {
                return;
            }

            long extracted = extractLongEnergy(amount, false);

            if (extracted <= 0L) {
                return;
            }

            int received = storage.receiveEnergy(
                    (int) Math.min(extracted, Integer.MAX_VALUE),
                    false
            );

            received = Math.max(
                    0,
                    Math.min(received, (int) extracted)
            );

            if (received < extracted) {
                energy = Math.min(
                        MAX_ENERGY,
                        energy + extracted - received
                );
            }

            sent[0] = received;

            setChanged();
            syncEnergy();
        });

        return sent[0];
    }

    @Override
    public long receiveLongEnergy(long maxReceive, boolean simulate) {
        if (maxReceive <= 0L) {
            return 0L;
        }

        long accepted = Math.min(
                Math.min(maxReceive, MAX_TRANSFER_PER_TICK),
                MAX_ENERGY - energy
        );

        if (!simulate && accepted > 0L) {
            energy += accepted;
            setChanged();
            syncEnergy();
        }

        return accepted;
    }

    @Override
    public long extractLongEnergy(long maxExtract, boolean simulate) {
        if (maxExtract <= 0L || energy <= 0L) {
            return 0L;
        }

        long extracted = Math.min(
                Math.min(maxExtract, MAX_TRANSFER_PER_TICK),
                energy
        );

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
        return true;
    }

    @Override
    public boolean canExtractLongEnergy() {
        return true;
    }

    @Override
    public <T> @NotNull LazyOptional<T> getCapability(
            @NotNull Capability<T> capability,
            @Nullable Direction side
    ) {
        if (capability == ForgeCapabilities.ENERGY) {
            return energyCapability.cast();
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
