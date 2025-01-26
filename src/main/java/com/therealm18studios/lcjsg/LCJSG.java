package com.therealm18studios.lcjsg;

import dev.architectury.platform.forge.EventBuses;
import li.cil.oc2.common.bus.device.provider.ProviderRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import li.cil.oc2.api.bus.device.provider.ItemDeviceProvider;
import net.minecraftforge.registries.DeferredRegister;


@Mod("lsjsg")
public class LCJSG {
    public LCJSG() {
        EventBuses.registerModEventBus("lsjsg", FMLJavaModLoadingContext.get().getModEventBus());
        Items.initialize();
        DeferredRegister<ItemDeviceProvider> registry;
        try {
            for (var field : ProviderRegistry.class.getDeclaredFields()) {
                if (field.getName().equals("ITEM_DEVICE_PROVIDERS")) {
                    field.setAccessible(true);
                    registry = (DeferredRegister<ItemDeviceProvider>) field.get(null);
                    registry.register("stargate_operations_module", StargateOperationsModuleDeviceProvider::new);
                    return;
                }
            }
        }
        catch (IllegalAccessException e) {
        }
        catch (NullPointerException n) {
        }
        catch (SecurityException e) {
        }

    }

}
