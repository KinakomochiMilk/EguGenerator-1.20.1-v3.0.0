
package com.kinakomochimilk.generatooooor;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

public class EguGeneratorMenu extends AbstractContainerMenu {

    private final BlockPos blockPos;
    private final EguGeneratorBlockEntity generator;

    private final DataSlot energyLow = DataSlot.standalone();
    private final DataSlot energyHigh = DataSlot.standalone();

    public EguGeneratorMenu(
            int containerId,
            Inventory inventory,
            BlockPos blockPos
    ) {
        super(ModMenus.EGU_GENERATOR.get(), containerId);

        this.blockPos = blockPos;

        BlockEntity blockEntity =
                inventory.player.level().getBlockEntity(blockPos);

        this.generator = blockEntity instanceof EguGeneratorBlockEntity entity
                ? entity
                : null;

        addDataSlot(energyLow);
        addDataSlot(energyHigh);

        if (generator != null) {
            updateEnergyData();
        }
    }

    public EguGeneratorMenu(
            int containerId,
            Inventory inventory,
            FriendlyByteBuf data
    ) {
        this(containerId, inventory, data.readBlockPos());
    }

    private void updateEnergyData() {
        if (generator == null) {
            energyLow.set(0);
            energyHigh.set(0);
            return;
        }

        long energy = generator.getEnergy();

        energyLow.set((int) energy);
        energyHigh.set((int) (energy >>> 32));
    }

    @Override
    public void broadcastChanges() {
        updateEnergyData();
        super.broadcastChanges();
    }

    public long getEnergy() {
        return (energyHigh.get() & 0xFFFFFFFFL) << 32
                | (energyLow.get() & 0xFFFFFFFFL);
    }

    /**
     * 現在の発電量を取得する。
     */
    public long getGenerationPerTick() {
        if (generator == null) {
            return 0L;
        }

        return generator.getGenerationPerTick();
    }

    public BlockPos getBlockPos() {
        return blockPos;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return player.distanceToSqr(
                blockPos.getX() + 0.5,
                blockPos.getY() + 0.5,
                blockPos.getZ() + 0.5
        ) <= 64.0;
    }
}
