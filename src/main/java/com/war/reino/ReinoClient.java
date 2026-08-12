package com.war.reino;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.renderer.entity.NoopRenderer;
import com.war.reino.mineros.EntidadesReino;
import com.war.reino.mineros.MineroRenderer;
import com.war.reino.constructores.Constructor;

public class ReinoClient implements ClientModInitializer {
    
    @Override
    public void onInitializeClient() {
        EntityRendererRegistry.register(EntidadesReino.MINERO, MineroRenderer::new);
        EntityRendererRegistry.register(EntidadesReino.CONSTRUCTOR, NoopRenderer::new);
        EntityRendererRegistry.register(EntidadesReino.ESPADACHIN, NoopRenderer::new);
        EntityRendererRegistry.register(EntidadesReino.ARQUERO, NoopRenderer::new);
        EntityRendererRegistry.register(EntidadesReino.CABALLERIA, NoopRenderer::new);
    }
}
