
package com.kinakomochimilk.generatooooor;

import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlockEntities {

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(
                    ForgeRegistries.BLOCK_ENTITY_TYPES,
                    EguGeneratorMod.MOD_ID
            );

    public static final RegistryObject<BlockEntityType<EguGeneratorBlockEntity>>
            EGU_GENERATOR =
            BLOCK_ENTITIES.register(
                    "egu_generator",
                    () -> BlockEntityType.Builder.of(
                            EguGeneratorBlockEntity::new,
                            ModBlocks.EGU_GENERATOR.get()
                    ).build(null)
            );

    public static final RegistryObject<BlockEntityType<EguCableBlockEntity>>
            EGU_CABLE =
            BLOCK_ENTITIES.register(
                    "egu_cable",
                    () -> BlockEntityType.Builder.of(
                            EguCableBlockEntity::new,
                            ModBlocks.EGU_CABLE.get()
                    ).build(null)
            );
}
