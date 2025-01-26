package com.therealm18studios.lcjsg;

import com.therealm18studios.lcjsg.manual.Manuals;
import dev.architectury.platform.forge.EventBuses;
import li.cil.oc2.common.bus.device.provider.ProviderRegistry;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import li.cil.oc2.api.bus.device.provider.ItemDeviceProvider;
import net.minecraftforge.registries.DeferredRegister;


@Mod("lcjsg")
public class LCJSG {
    public static String MOD_ID = "lcjsg";
    public LCJSG() {
        EventBuses.registerModEventBus(MOD_ID, FMLJavaModLoadingContext.get().getModEventBus());
        Items.initialize();
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> Manuals::initialize);
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
