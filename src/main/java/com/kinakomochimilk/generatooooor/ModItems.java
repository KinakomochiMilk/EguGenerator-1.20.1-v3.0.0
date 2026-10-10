
package com.kinakomochimilk.generatooooor;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(
                    ForgeRegistries.ITEMS,
                    EguGeneratorMod.MOD_ID
            );

    public static final RegistryObject<Item> EGU_GENERATOR =
            ITEMS.register(
                    "egu_generator",
                    () -> new BlockItem(
                            ModBlocks.EGU_GENERATOR.get(),
                            new Item.Properties()
                    )
            );

    public static final RegistryObject<Item> EGU_CABLE =
            ITEMS.register(
                    "egu_cable",
                    () -> new BlockItem(
                            ModBlocks.EGU_CABLE.get(),
                            new Item.Properties()
                    )
            );

    public static final RegistryObject<Item> COMPRESSED_MAGMA_BLOCK =
            ITEMS.register(
                    "compressed_magma_block",
                    () -> new BlockItem(
                            ModBlocks.COMPRESSED_MAGMA_BLOCK.get(),
                            new Item.Properties()
                    )
            );

    public static final RegistryObject<Item> DOUBLE_COMPRESSED_MAGMA_BLOCK =
            ITEMS.register(
                    "double_compressed_magma_block",
                    () -> new BlockItem(
                            ModBlocks.DOUBLE_COMPRESSED_MAGMA_BLOCK.get(),
                            new Item.Properties()
                    )
            );

    public static final RegistryObject<Item> TRIPLE_COMPRESSED_MAGMA_BLOCK =
            ITEMS.register(
                    "triple_compressed_magma_block",
                    () -> new BlockItem(
                            ModBlocks.TRIPLE_COMPRESSED_MAGMA_BLOCK.get(),
                            new Item.Properties()
                    )
            );
}
