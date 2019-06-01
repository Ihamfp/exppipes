package ihamfp.exppipes.client.render;

import ihamfp.exppipes.Utils;
import ihamfp.exppipes.blocks.BlockStackDisplay;
import ihamfp.exppipes.tileentities.TileEntityStackDisplay;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;

public class TESRStackDisplay extends TileEntitySpecialRenderer<TileEntityStackDisplay> {
	static final double itemScale = 0.75;
	static final double textScale = 0.75;
	
	@Override
	public void render(TileEntityStackDisplay te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
		if (!te.displayedStack.isEmpty()) {
			GlStateManager.pushMatrix();
			GlStateManager.translate(x+0.5, y+0.5, z+0.5);
			//GlStateManager.translate(0.0, 1.0, -1.0);
			
			IBlockState blockState = te.getWorld().getBlockState(te.getPos());
			GlStateManager.rotate(-blockState.getValue(BlockStackDisplay.yaw).getHorizontalAngle(), 0.0f, 1.0f, 0.0f);
			GlStateManager.rotate(Utils.getPitchAngle(blockState.getValue(BlockStackDisplay.pitch)), 1.0f, 0.0f, 0.0f);
			GlStateManager.translate(0, 0, -0.501);
			
			OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0f, 240.0f);
			
			final RenderItem renderItem = Minecraft.getMinecraft().getRenderItem();
			
			GlStateManager.rotate(180.0f, 0.0f, 1.0f, 0.0f);
			GlStateManager.scale(itemScale, itemScale, 0.0001);
			renderItem.renderItem(te.displayedStack, TransformType.GUI);
			
			GlStateManager.rotate(180.0f, 0.0f, 1.0f, 0.0f);
			GlStateManager.rotate(180.0f, 0.0f, 0.0f, 1.0f);
			GlStateManager.scale(1/itemScale, 1/itemScale, 1.0f);
			
			FontRenderer fr = Minecraft.getMinecraft().fontRenderer;
			int strw = fr.getStringWidth(te.displayedText);

			GlStateManager.translate(0.0, textScale/2, -1.0);
			
			GlStateManager.scale(textScale/strw, textScale/strw, 10000);
			GlStateManager.translate(-strw/2, -fr.FONT_HEIGHT, -0.005);
			
			// found all these enable/disable in the StorageDrawer code (MIT license)
			GlStateManager.disableLighting();
			GlStateManager.enablePolygonOffset();
			GlStateManager.depthMask(false);
			GlStateManager.enableBlend();
			
			fr.drawString(te.displayedText, 0, 0, 0xff404040);
			GlStateManager.disableBlend();
			GlStateManager.depthMask(true);
			GlStateManager.disablePolygonOffset();
			GlStateManager.enableLighting();
			
			GlStateManager.disableAlpha();
			
			GlStateManager.popMatrix();
		}
		super.render(te, x, y, z, partialTicks, destroyStage, alpha);
	}
}
