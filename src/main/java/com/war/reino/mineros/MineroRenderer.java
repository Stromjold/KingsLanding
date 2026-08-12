package com.war.reino.mineros;

import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.model.BipedModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.resources.Identifier;

public class MineroRenderer extends MobRenderer<MineroEntity, MineroRenderState, BipedModel<MineroRenderState>> {
    
    // Aquí le decimos dónde está guardada tu imagen
    private static final Identifier TEXTURA = Identifier.parse("reino:textures/entity/minero.png");

    public MineroRenderer(EntityRendererProvider.Context context) {
        // Le damos la forma 3D de un humanoide (Bípedo) y una sombra de tamaño 0.5
        super(context, new BipedModel<>(context.bakeLayer(ModelLayers.ZOMBIE)), 0.5f);
    }

    // Le entrega la textura al juego
    @Override
    public Identifier getTextureLocation(MineroRenderState state) {
        return TEXTURA;
    }

    // Crea el estado visual
    @Override
    public MineroRenderState createRenderState() {
        return new MineroRenderState();
    }

    // Extrae los datos de la entidad (como si está caminando o quieto) para animarlo
    @Override
    public void extractRenderState(MineroEntity entity, MineroRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
    }
}
