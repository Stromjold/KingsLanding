package com.war.reino.mixin;

import com.war.reino.Reino;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
public class ExampleMixin {
	@Inject(at = @At("TAIL"), method = "loadLevel")
	private void init(CallbackInfo info) {
		Reino.LOGGER.info("El mundo ha terminado de cargar y el mixin del Reino está activo.");
	}
}