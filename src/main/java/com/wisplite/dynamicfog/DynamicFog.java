package com.wisplite.dynamicfog;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;
import com.wisplite.dynamicfog.blocks.FogGeneratorBlock;
import com.wisplite.dynamicfog.blocks.FogGeneratorBlockEntity;

import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import java.util.function.Supplier;
import com.wisplite.dynamicfog.menus.FogGeneratorMenu;
import com.wisplite.dynamicfog.particles.DynamicFogParticleTypes;
import com.wisplite.dynamicfog.particles.FogParticle;
import com.wisplite.dynamicfog.payloads.FogGeneratorUpdatePayload;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(DynamicFog.MODID)
public class DynamicFog {
    // Define mod id in a common place for everything to reference
    public static final String MODID = "dynamicfog";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();
    // Create a Deferred Register to hold Blocks which will all be registered under the "dynamicfog" namespace
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);
    // Create a Deferred Register to hold Items which will all be registered under the "dynamicfog" namespace
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);

    public static final DeferredRegister<ParticleType<?>> PARTICLES = DeferredRegister.create(Registries.PARTICLE_TYPE, MODID);
    
    public static final Supplier<SimpleParticleType> FOG_PARTICLE = PARTICLES.register("fog", () -> new SimpleParticleType(true));

    // Create a Deferred Register to hold CreativeModeTabs which will all be registered under the "dynamicfog" namespace
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);
    
    public static final DeferredBlock<FogGeneratorBlock> FOG_GENERATOR_BLOCK =
        BLOCKS.registerBlock("fog_generator",
            FogGeneratorBlock::new,
            BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(3.0f));

    public static final DeferredItem<BlockItem> FOG_GENERATOR_ITEM =
        ITEMS.registerSimpleBlockItem(FOG_GENERATOR_BLOCK);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> DYNAMIC_FOG_TAB =
        CREATIVE_MODE_TABS.register("dynamicfog", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.dynamicfog"))
            .icon(() -> new ItemStack(FOG_GENERATOR_ITEM.get()))
            .displayItems((params, output) -> output.accept(FOG_GENERATOR_ITEM.get()))
            .build());

    public static final DeferredRegister<MenuType<?>> MENUS = 
        DeferredRegister.create(Registries.MENU, MODID);

    public static final Supplier<MenuType<FogGeneratorMenu>> FOG_GENERATOR_MENU =
        MENUS.register("fog_generator", () -> IMenuTypeExtension.create(FogGeneratorMenu::new));

    // The constructor for the mod class is the first code that is run when your mod is loaded.
    // FML will recognize some parameter types like IEventBus or ModContainer and pass them in automatically.
    public DynamicFog(IEventBus modEventBus, ModContainer modContainer) {
        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);

        // Register the Deferred Register to the mod event bus so blocks get registered
        BLOCKS.register(modEventBus);
        FogGeneratorBlockEntity.BLOCK_ENTITY_TYPES.register(modEventBus);
        // Register the Deferred Register to the mod event bus so items get registered
        ITEMS.register(modEventBus);
        //PARTICLES.register(modEventBus);
        // Register the Deferred Register to the mod event bus so tabs get registered
        CREATIVE_MODE_TABS.register(modEventBus);
        DynamicFogParticleTypes.PARTICLES.register(modEventBus);
        MENUS.register(modEventBus);
        modEventBus.addListener(DynamicFog::register);
        //modEventBus.addListener(DynamicFog::registerParticles);

        // Register ourselves for server and other game events we are interested in.
        // Note that this is necessary if and only if we want *this* class (DynamicFog) to respond directly to events.
        // Do not add this line if there are no @SubscribeEvent-annotated functions in this class, like onServerStarting() below.
        NeoForge.EVENT_BUS.register(this);

        // Register our mod's ModConfigSpec so that FML can create and load the config file for us
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        // Some common setup code
        LOGGER.info("HELLO FROM COMMON SETUP");

        if (Config.LOG_DIRT_BLOCK.getAsBoolean()) {
            LOGGER.info("DIRT BLOCK >> {}", BuiltInRegistries.BLOCK.getKey(Blocks.DIRT));
        }

        LOGGER.info("{}{}", Config.MAGIC_NUMBER_INTRODUCTION.get(), Config.MAGIC_NUMBER.getAsInt());

        Config.ITEM_STRINGS.get().forEach((item) -> LOGGER.info("ITEM >> {}", item));
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        // Do something when the server starts
        LOGGER.info("DynamicFog ready!");
    }

    public static void register(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("1.0");

        registrar.playToServer(FogGeneratorUpdatePayload.TYPE, FogGeneratorUpdatePayload.CODEC, (payload, context) -> payload.handle(context));
    }

    public static void registerParticles(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(FOG_PARTICLE.get(), FogParticle.Provider::new);
    }
}