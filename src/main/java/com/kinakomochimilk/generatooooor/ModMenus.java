package com.kinakomochimilk.generatooooor;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModMenus {

    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(ForgeRegistries.MENU_TYPES, EguGeneratorMod.MOD_ID);

    public static final RegistryObject<MenuType<EguGeneratorMenu>> EGU_GENERATOR =
            MENUS.register("egu_generator",
                    () -> IForgeMenuType.create(EguGeneratorMenu::new));
}
