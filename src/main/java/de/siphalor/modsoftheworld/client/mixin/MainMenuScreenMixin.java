package de.siphalor.modsoftheworld.client.mixin;

import com.mojang.blaze3d.platform.GlStateManager;
import de.siphalor.modsoftheworld.client.ClientCore;
import de.siphalor.modsoftheworld.client.LogoTexture;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.network.chat.Component;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public abstract class MainMenuScreenMixin extends Screen {

	private int modsOfTheWorld_currentLogo = 0;
	private float modsOfTheWorld_logoTime = 0;

	protected MainMenuScreenMixin(Component component_1) {
		super(component_1);
	}

	@Inject(method = "render", at = @At("HEAD"))
	public void onRender(int x, int y, float delta, CallbackInfo callbackInfo) {
		modsOfTheWorld_logoTime += delta / 4;
	}

	@Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/TitleScreen;blit(IIFFIIII)V", ordinal = 0))
	public void editionBlitProxy(int x, int y, float texX, float texY, int width, int height, int texWidth, int texHeight) {
		blit(x + 39, y, texX + 39, texY, width - 39, height, texWidth, texHeight);

		if(modsOfTheWorld_logoTime > ClientCore.WHOLE_TIME)
			modsOfTheWorld_currentLogo = modsOfTheWorld_currentLogo >= ClientCore.getLogos().size() - 1 ? 0 : modsOfTheWorld_currentLogo + 1;
		modsOfTheWorld_logoTime %= ClientCore.WHOLE_TIME;
		float[] color = new float[4];
		GL11.glGetFloatv(GL11.GL_CURRENT_COLOR, color);
		if(color[3] == 1.0F) {
			float alpha = 1.0F;
			if (modsOfTheWorld_logoTime < ClientCore.FADE_TIME) alpha = modsOfTheWorld_logoTime / ClientCore.FADE_TIME;
			if (modsOfTheWorld_logoTime >= ClientCore.WHOLE_TIME - ClientCore.FADE_TIME)
				alpha = 1.0F - (modsOfTheWorld_logoTime - ClientCore.WHOLE_TIME + ClientCore.FADE_TIME) / ClientCore.FADE_TIME;
			GlStateManager.color4f(1.0F, 1.0F, 1.0F, alpha);
		}

		GlStateManager.pushMatrix();

		LogoTexture logoTexture = ClientCore.getLogos().get(modsOfTheWorld_currentLogo);
		minecraft.getTextureManager().bindTexture(logoTexture.identifier);

		float scaleFactor = (float) height / (float) logoTexture.height;
		GlStateManager.translatef(x + 39 - logoTexture.width * scaleFactor, y, 0.0F);
		GlStateManager.scalef(scaleFactor, scaleFactor, 1.0F);
		blit(0, 0, 0.0F, 0.0F, logoTexture.width, logoTexture.height, logoTexture.width, logoTexture.height);

        if(color[3] == 1.0F)
        	GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		GlStateManager.popMatrix();
	}

	private void renderLogoTexture(float x, float y, int width, int height, LogoTexture texture) {
		float textureOffset = 0;
		float actualWidth = width;
		if(x < 0) {
			actualWidth += x;
			textureOffset = -x;
			x = 0;
		}
		if(x + width > 274) {
			actualWidth -= width + x - 274;
		}
		this.minecraft.getTextureManager().bindTexture(texture.identifier);
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder bufferBuilder = tessellator.getBufferBuilder();
		bufferBuilder.begin(7, VertexFormats.POSITION_UV);
		bufferBuilder.vertex(x, height, 0).texture(textureOffset / width, 1).next();
		bufferBuilder.vertex(x + actualWidth, height, 0).texture((textureOffset + actualWidth) / width, 1).next();
		bufferBuilder.vertex(x + actualWidth, 0, 0).texture((textureOffset + actualWidth) / width, 0).next();
		bufferBuilder.vertex(x, 0, 0).texture(textureOffset / width, 0).next();
		tessellator.draw();
	}
}
