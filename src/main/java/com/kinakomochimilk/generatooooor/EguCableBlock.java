
package com.kinakomochimilk.generatooooor;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class EguCableBlock extends BaseEntityBlock {

    public static final BooleanProperty NORTH =
            BooleanProperty.create("north");
    public static final BooleanProperty SOUTH =
            BooleanProperty.create("south");
    public static final BooleanProperty EAST =
            BooleanProperty.create("east");
    public static final BooleanProperty WEST =
            BooleanProperty.create("west");
    public static final BooleanProperty UP =
            BooleanProperty.create("up");
    public static final BooleanProperty DOWN =
            BooleanProperty.create("down");

    private static final VoxelShape CORE = Block.box(
            5, 5, 5, 11, 11, 11
    );

    private static final VoxelShape NORTH_ARM = Block.box(
            5, 5, 0, 11, 11, 5
    );

    private static final VoxelShape SOUTH_ARM = Block.box(
            5, 5, 11, 11, 11, 16
    );

    private static final VoxelShape WEST_ARM = Block.box(
            0, 5, 5, 5, 11, 11
    );

    private static final VoxelShape EAST_ARM = Block.box(
            11, 5, 5, 16, 11, 11
    );

    private static final VoxelShape DOWN_ARM = Block.box(
            5, 0, 5, 11, 5, 11
    );

    private static final VoxelShape UP_ARM = Block.box(
            5, 11, 5, 11, 16, 11
    );

    public EguCableBlock() {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(2.0F, 6.0F)
                .sound(SoundType.METAL));

        registerDefaultState(
                stateDefinition.any()
                        .setValue(NORTH, false)
                        .setValue(SOUTH, false)
                        .setValue(EAST, false)
                        .setValue(WEST, false)
                        .setValue(UP, false)
                        .setValue(DOWN, false)
        );
    }

    @Override
    protected void createBlockStateDefinition(
            StateDefinition.Builder<Block, BlockState> builder
    ) {
        builder.add(NORTH, SOUTH, EAST, WEST, UP, DOWN);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return updateConnections(
                context.getLevel(),
                context.getClickedPos(),
                defaultBlockState()
        );
    }

    @Override
    public void neighborChanged(
            BlockState state,
            Level level,
            BlockPos pos,
            Block neighborBlock,
            BlockPos neighborPos,
            boolean movedByPiston
    ) {
        super.neighborChanged(
                state,
                level,
                pos,
                neighborBlock,
                neighborPos,
                movedByPiston
        );

        if (!level.isClientSide) {
            BlockState updated = updateConnections(level, pos, state);

            if (!updated.equals(state)) {
                level.setBlock(pos, updated, 3);
            }
        }
    }

    public void refreshConnections(Level level, BlockPos pos) {
        if (level.isClientSide) {
            return;
        }

        BlockState state = level.getBlockState(pos);

        if (!state.is(this)) {
            return;
        }

        BlockState updated = updateConnections(level, pos, state);

        if (!updated.equals(state)) {
            level.setBlock(pos, updated, 3);
        }
    }

    private BlockState updateConnections(
            Level level,
            BlockPos pos,
            BlockState state
    ) {
        return state
                .setValue(NORTH, connectsTo(level, pos, Direction.NORTH))
                .setValue(SOUTH, connectsTo(level, pos, Direction.SOUTH))
                .setValue(EAST, connectsTo(level, pos, Direction.EAST))
                .setValue(WEST, connectsTo(level, pos, Direction.WEST))
                .setValue(UP, connectsTo(level, pos, Direction.UP))
                .setValue(DOWN, connectsTo(level, pos, Direction.DOWN));
    }

    private boolean connectsTo(
            Level level,
            BlockPos pos,
            Direction direction
    ) {
        BlockPos targetPos = pos.relative(direction);
        BlockState targetState = level.getBlockState(targetPos);

        // ケーブル同士は接続
        if (targetState.is(ModBlocks.EGU_CABLE.get())) {
            return true;
        }

        // 発電機は出力面だけ接続
        if (targetState.is(ModBlocks.EGU_GENERATOR.get())) {
            Direction facing = targetState.getValue(
                    EguGeneratorBlock.FACING
            );

            Direction outputDirection = facing;

            // ケーブルから発電機へ向かう方向は出力方向の反対
            return direction == outputDirection.getOpposite();
        }

        BlockEntity target = level.getBlockEntity(targetPos);

        if (target == null) {
            return false;
        }

        // 独自のlong型エネルギーAPI
        if (target instanceof ILongEnergyStorage longStorage) {
            if (longStorage.canReceiveLongEnergy()
                    || longStorage.canExtractLongEnergy()) {
                return true;
            }
        }

        // Forge Energy API
        return target.getCapability(
                        net.minecraftforge.common.capabilities
                                .ForgeCapabilities.ENERGY,
                        direction.getOpposite()
                )
                .map(storage ->
                        storage.canReceive() || storage.canExtract()
                )
                .orElse(false);
    }

    @Override
    public BlockEntity newBlockEntity(
            BlockPos pos,
            BlockState state
    ) {
        return new EguCableBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            Level level,
            BlockState state,
            BlockEntityType<T> type
    ) {
        return createTickerHelper(
                type,
                ModBlockEntities.EGU_CABLE.get(),
                (world, pos, blockState, blockEntity) ->
                        blockEntity.serverTick()
        );
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    private VoxelShape getCableShape(BlockState state) {
        VoxelShape shape = CORE;

        if (state.getValue(NORTH)) {
            shape = Shapes.or(shape, NORTH_ARM);
        }

        if (state.getValue(SOUTH)) {
            shape = Shapes.or(shape, SOUTH_ARM);
        }

        if (state.getValue(EAST)) {
            shape = Shapes.or(shape, EAST_ARM);
        }

        if (state.getValue(WEST)) {
            shape = Shapes.or(shape, WEST_ARM);
        }

        if (state.getValue(UP)) {
            shape = Shapes.or(shape, UP_ARM);
        }

        if (state.getValue(DOWN)) {
            shape = Shapes.or(shape, DOWN_ARM);
        }

        return shape;
    }

    @Override
    public VoxelShape getShape(
            BlockState state,
            BlockGetter level,
            BlockPos pos,
            CollisionContext context
    ) {
        return getCableShape(state);
    }

    @Override
    public VoxelShape getCollisionShape(
            BlockState state,
            BlockGetter level,
            BlockPos pos,
            CollisionContext context
    ) {
        return getCableShape(state);
    }

    @Override
    public VoxelShape getOcclusionShape(
            BlockState state,
            BlockGetter level,
            BlockPos pos
    ) {
        return getCableShape(state);
    }

    @Override
    public InteractionResult use(
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            InteractionHand hand,
            BlockHitResult hit
    ) {
        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResult.PASS;
        }

        if (!level.isClientSide) {
            EguCableNetworkInfo.printNetworkInfo(
                    level,
                    pos,
                    player
            );
        }

        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
