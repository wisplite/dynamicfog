package com.wisplite.dynamicfog.particles;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.client.particle.ParticleProvider;

public class FogParticle extends TextureSheetParticle {

    protected FogParticle(ClientLevel level, double x, double y, double z, SpriteSet sprites) {
        super(level, x, y, z);
        
        // Disable physics collision so it doesn't waste CPU trying to bounce off walls
        this.hasPhysics = false; 
        
        // Massive scale! (Between 2.0 and 3.0 blocks wide)
        this.quadSize = 2.0F + this.random.nextFloat() * 1.0F; 
        
        // Long lifetime! (Lives for 100 to 160 ticks, which is 5 to 8 seconds)
        this.lifetime = 100 + this.random.nextInt(60); 
        
        // Starting opacity
        this.alpha = 0.0F; 
        
        // Give it a tiny, slow drift
        this.xd = (this.random.nextDouble() - 0.5) * 0.02;
        this.yd = (this.random.nextDouble() - 0.5) * 0.02;
        this.zd = (this.random.nextDouble() - 0.5) * 0.02;

        this.pickSprite(sprites);
    }

    @Override
    public ParticleRenderType getRenderType() {
        return new ParticleRenderType() {
            @Override
            public BufferBuilder begin(Tesselator tesselator, TextureManager textureManager) {
                RenderSystem.depthMask(false); // Stop particles from clipping each other
                RenderSystem.enableBlend();
                // Force Additive Blending (GL_SRC_ALPHA, GL_ONE)
                RenderSystem.blendFunc(com.mojang.blaze3d.platform.GlStateManager.SourceFactor.SRC_ALPHA, com.mojang.blaze3d.platform.GlStateManager.DestFactor.ONE);
                RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_PARTICLES);
                return tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.PARTICLE);
            }

            @Override
            public String toString() {
                return "dynamicfog:fog";
            }
        };
    }

    @Override
    public void tick() {
        super.tick();
        
        // Custom logic to make the particle fade in smoothly, then fade out before it dies
        float ageRatio = (float) this.age / (float) this.lifetime;
        
        if (ageRatio < 0.2F) {
            // Fade in over the first 20% of its life (max alpha 0.2 so it stays foggy)
            this.alpha = ageRatio * 1.0F; 
        } else if (ageRatio > 0.7F) {
            // Fade out over the last 30% of its life
            this.alpha = 0.2F * (1.0F - ((ageRatio - 0.7F) / 0.3F));
        } else {
            this.alpha = 0.2F;
        }
    }

    // The Factory is required to tell the game how to spawn this specific particle
    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public FogParticle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double dx, double dy, double dz) {
            return new FogParticle(level, x, y, z, this.sprites);
        }
    }
}