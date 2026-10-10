
package com.kinakomochimilk.generatooooor;

import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(EguGeneratorMod.MOD_ID)
public class EguGeneratorMod {

    public static final String MOD_ID = "generatooooor";

    public EguGeneratorMod() {
        IEventBus modEventBus =
                FMLJavaModLoadingContext.get().getModEventBus();

        ModBlocks.BLOCKS.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModBlockEntities.BLOCK_ENTITIES.register(modEventBus);
        ModMenus.MENUS.register(modEventBus);

        modEventBus.addListener(this::addCreative);
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
            event.accept(ModItems.EGU_GENERATOR);
            event.accept(ModItems.EGU_CABLE);
            event.accept(ModItems.COMPRESSED_MAGMA_BLOCK);
            event.accept(ModItems.DOUBLE_COMPRESSED_MAGMA_BLOCK);
            event.accept(ModItems.TRIPLE_COMPRESSED_MAGMA_BLOCK);
        }
    }
}
