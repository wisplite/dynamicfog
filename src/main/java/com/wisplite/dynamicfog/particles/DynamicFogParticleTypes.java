package com.wisplite.dynamicfog.particles;

import com.wisplite.dynamicfog.DynamicFog;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.neoforge.registries.DeferredRegister;
import team.lodestar.lodestone.systems.particle.world.type.LodestoneWorldParticleType;

import java.util.function.Supplier;

public class DynamicFogParticleTypes {
    public static final DeferredRegister<ParticleType<?>> PARTICLES =
        DeferredRegister.create(BuiltInRegistries.PARTICLE_TYPE, DynamicFog.MODID);

    public static final Supplier<LodestoneWorldParticleType> FOG =
        PARTICLES.register("fog", LodestoneWorldParticleType::new);
}