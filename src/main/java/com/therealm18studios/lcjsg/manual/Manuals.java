package com.therealm18studios.lcjsg.manual;

import com.therealm18studios.lcjsg.LCJSG;
import com.therealm18studios.lcjsg.Items;
import li.cil.manual.api.ManualModel;
import li.cil.manual.api.Tab;
import li.cil.manual.api.prefab.Manual;
import li.cil.manual.api.prefab.provider.NamespaceDocumentProvider;
import li.cil.manual.api.prefab.provider.NamespacePathProvider;
import li.cil.manual.api.prefab.tab.ItemStackTab;
import li.cil.manual.api.provider.DocumentProvider;
import li.cil.manual.api.provider.PathProvider;
import li.cil.manual.api.util.Constants;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

@OnlyIn(Dist.CLIENT)
public final class Manuals {

    private static final DeferredRegister<ManualModel> MANUALS = DeferredRegister.create(Constants.MANUAL_REGISTRY, Constants.MOD_ID);
    private static final DeferredRegister<PathProvider> PATH_PROVIDERS = DeferredRegister.create(Constants.PATH_PROVIDER_REGISTRY, Constants.MOD_ID);
    private static final DeferredRegister<DocumentProvider> CONTENT_PROVIDERS = DeferredRegister.create(Constants.DOCUMENT_PROVIDER_REGISTRY, Constants.MOD_ID);
    private static final DeferredRegister<Tab> TABS = DeferredRegister.create(Constants.TAB_REGISTRY, Constants.MOD_ID);

    ///////////////////////////////////////////////////////////////////

    public static final RegistryObject<ManualModel> MANUAL = MANUALS.register("manual", Manual::new);

    ///////////////////////////////////////////////////////////////////

    public static void initialize() {
        MANUALS.register(FMLJavaModLoadingContext.get().getModEventBus());

        PATH_PROVIDERS.register(FMLJavaModLoadingContext.get().getModEventBus());
        CONTENT_PROVIDERS.register(FMLJavaModLoadingContext.get().getModEventBus());
        TABS.register(FMLJavaModLoadingContext.get().getModEventBus());

//        PATH_PROVIDERS.register("path_provider", () -> new NamespacePathProvider(LCJSG.MOD_ID));
//        CONTENT_PROVIDERS.register("content_provider", () -> new NamespaceDocumentProvider(LCJSG.MOD_ID, "doc"));

        TABS.register("lcjsg", () -> new ItemStackTab(
            ManualModel.LANGUAGE_KEY + "/lcjsg/index.md",
            Component.translatable("manual." + LCJSG.MOD_ID + ".items"),
            new ItemStack(Items.STARGATE_OPERATIONS_MODULE.get())));
    }
}
