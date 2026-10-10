package com.kinakomochimilk.generatooooor;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;

public final class EguCableNetworkInfo {

    private EguCableNetworkInfo() {
    }

    /**
     * 接続されたえぐケーブルを探索し、
     * 接続数と総蓄電量のみを表示する。
     */
    public static void printNetworkInfo(
            Level level,
            BlockPos startPos,
            Player player
    ) {
        if (level.isClientSide) {
            return;
        }

        Set<BlockPos> visited = new HashSet<>();
        Queue<BlockPos> queue = new ArrayDeque<>();

        long totalEnergy = 0L;

        queue.add(startPos);

        while (!queue.isEmpty()) {
            BlockPos pos = queue.remove();

            if (!visited.add(pos)) {
                continue;
            }

            BlockEntity blockEntity = level.getBlockEntity(pos);

            if (!(blockEntity instanceof EguCableBlockEntity cable)) {
                continue;
            }

            totalEnergy = saturatingAdd(
                    totalEnergy,
                    cable.getLongEnergyStored()
            );

            for (Direction direction : Direction.values()) {
                BlockPos neighborPos = pos.relative(direction);

                if (!visited.contains(neighborPos)
                        && level.getBlockState(neighborPos)
                        .is(ModBlocks.EGU_CABLE.get())) {
                    queue.add(neighborPos);
                }
            }
        }

        int cableCount = 0;

        for (BlockPos pos : visited) {
            if (level.getBlockState(pos).is(ModBlocks.EGU_CABLE.get())) {
                cableCount++;
            }
        }

        player.sendSystemMessage(
                Component.literal("接続数: " + cableCount + " 本")
        );

        player.sendSystemMessage(
                Component.literal(
                        "総蓄電量: " + formatEnergy(totalEnergy)
                )
        );
    }

    private static long saturatingAdd(long a, long b) {
        if (b > 0L && a > Long.MAX_VALUE - b) {
            return Long.MAX_VALUE;
        }

        return a + b;
    }

    private static String formatEnergy(long energy) {
        if (energy >= 1_000_000_000_000L) {
            return String.format(
                    java.util.Locale.ROOT,
                    "%.2f TFE",
                    energy / 1_000_000_000_000.0
            );
        }

        if (energy >= 1_000_000_000L) {
            return String.format(
                    java.util.Locale.ROOT,
                    "%.2f GFE",
                    energy / 1_000_000_000.0
            );
        }

        if (energy >= 1_000_000L) {
            return String.format(
                    java.util.Locale.ROOT,
                    "%.2f MFE",
                    energy / 1_000_000.0
            );
        }

        if (energy >= 1_000L) {
            return String.format(
                    java.util.Locale.ROOT,
                    "%.2f kFE",
                    energy / 1_000.0
            );
        }

        return energy + " FE";
    }
}