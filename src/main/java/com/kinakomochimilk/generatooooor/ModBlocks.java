package com.kinakomochimilk.generatooooor;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlocks {

    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(
                    ForgeRegistries.BLOCKS,
                    EguGeneratorMod.MOD_ID
            );

    public static final RegistryObject<Block> EGU_GENERATOR =
            BLOCKS.register(
                    "egu_generator",
                    EguGeneratorBlock::new
            );

    public static final RegistryObject<Block> EGU_CABLE =
            BLOCKS.register(
                    "egu_cable",
                    EguCableBlock::new
            );

    public static final RegistryObject<Block> COMPRESSED_MAGMA_BLOCK =
            BLOCKS.register(
                    "compressed_magma_block",
                    () -> new Block(
                            BlockBehaviour.Properties.of()
                                    .mapColor(MapColor.FIRE)
                                    .strength(2.0F, 6.0F)
                                    .requiresCorrectToolForDrops()
                                    .lightLevel(state -> 15)
                                    .sound(SoundType.STONE)
                    )
            );

    public static final RegistryObject<Block> DOUBLE_COMPRESSED_MAGMA_BLOCK =
            BLOCKS.register(
                    "double_compressed_magma_block",
                    () -> new Block(
                            BlockBehaviour.Properties.of()
                                    .mapColor(MapColor.FIRE)
                                    .strength(4.0F, 12.0F)
                                    .requiresCorrectToolForDrops()
                                    .lightLevel(state -> 15)
                                    .sound(SoundType.STONE)
                    )
            );

    public static final RegistryObject<Block> TRIPLE_COMPRESSED_MAGMA_BLOCK =
            BLOCKS.register(
                    "triple_compressed_magma_block",
                    () -> new Block(
                            BlockBehaviour.Properties.of()
                                    .mapColor(MapColor.FIRE)
                                    .strength(8.0F, 24.0F)
                                    .requiresCorrectToolForDrops()
                                    .lightLevel(state -> 15)
                                    .sound(SoundType.STONE)
                    )
            );
}