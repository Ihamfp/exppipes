package ihamfp.exppipes.containers;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Map;

import ihamfp.exppipes.ExppipesMod;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidTank;

public abstract class GuiContainerDecorated extends GuiContainer {
	
	public static final ResourceLocation decoration = new ResourceLocation(ExppipesMod.MODID, "textures/gui/decoration.png");

	public GuiContainerDecorated(Container inventorySlotsIn) {
		super(inventorySlotsIn);
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		this.drawDefaultBackground();
		super.drawScreen(mouseX, mouseY, partialTicks);
		this.renderHoveredToolTip(mouseX, mouseY);
	}
	
	public void drawLiquidStorageLevel(int x, int y, int mouseX, int mouseY, FluidTank fs) {
		int fluidLevel = 33*fs.getFluidAmount()/fs.getCapacity();
		mc.getTextureManager().bindTexture(decoration);
		drawTexturedModalRect(x, y, 40, 0, 20, 35);
		// draw fluid texture here
		if (fs.getFluidAmount() > 0) {
			TextureAtlasSprite fluidTexture = mc.getTextureMapBlocks().getTextureExtry(fs.getFluid().getFluid().getStill().toString());
			if (fluidTexture == null) {
				fluidTexture = mc.getTextureMapBlocks().getTextureExtry(FluidRegistry.WATER.getStill().toString());
			}
			mc.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
			drawTexturedModalRect(x+2, y+33-fluidLevel, fluidTexture, 16, fluidLevel);
			
			mc.getTextureManager().bindTexture(decoration);
			drawTexturedModalRect(x+2, y+33-fluidLevel, 60, 33-fluidLevel, 16, fluidLevel);
		}
		
		if ((mouseX >= x && mouseX <= x+20) && (mouseY >= y && mouseY <= y+35)) {
			ArrayList<String> text = new ArrayList<String>();
			if (fs.getFluidAmount() > 0)
				text.add(fs.getFluid().getLocalizedName());
			text.add(fs.getFluidAmount() + "/" + fs.getCapacity() + " mb");
			drawHoveringText(text, mouseX, mouseY);
		}
	}
	
	public void drawBurningLevel(int x, int y, int level, int levelMax) {
		int burnLevel = 13*level/levelMax;
		mc.getTextureManager().bindTexture(decoration);
		drawTexturedModalRect(x+1, y+1, 20, 41, 13, 13);
		drawTexturedModalRect(x, y+13-burnLevel, 34, 53-burnLevel, 13, burnLevel);
	}
	
	public void drawSlot(int x, int y) {
		mc.getTextureManager().bindTexture(decoration);
		drawTexturedModalRect(x, y, 1, 36, 18, 18);
	}
	
	public void drawLargeSlot(int x, int y) {
		mc.getTextureManager().bindTexture(decoration);
		drawTexturedModalRect(x, y, 49, 36, 26, 26);
	}
	
	public void drawVertProgressBar(int x, int y, int progressMax, int progress, int color) {
		if (progressMax == 0) return;
		int level = 16*progress/progressMax;
		mc.getTextureManager().bindTexture(decoration);
		drawTexturedModalRect(x, y, 81, 36, 3, 18);
		
		if (progressMax > 0 && progress > 0) {
			Color col = new Color(color);
			int red = col.getRed();
			int green = col.getGreen();
			int blue = col.getBlue();
			GlStateManager.color(red/255.0f, green/255.0f, blue/255.0f);
			drawTexturedModalRect(x+1, y+17-level, 84, 53-level, 1, level);
			GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
		}
	}
	
	public void drawLargeVertProgressBar(int x, int y, int progressMax, int progress, int color) {
		if (progressMax == 0) return;
		int level = 24*progress/progressMax;
		mc.getTextureManager().bindTexture(decoration);
		drawTexturedModalRect(x, y, 76, 36, 3, 26);
		if (progressMax > 0 && progress > 0) {
			Color col = new Color(color);
			int red = col.getRed();
			int green = col.getGreen();
			int blue = col.getBlue();
			GlStateManager.color(red/255.0f, green/255.0f, blue/255.0f);
			drawTexturedModalRect(x+1, y+25-level, 77, 60-level, 1, level);
			GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
		}
	}
	
	public void drawItemSelector(int x, int y, int gridW, int gridH, Map<ItemStack,Integer> stacks, int selected, int page, int mouseX, int mouseY) {
		int itemsPerPage = gridW*gridH;
		
		if (selected >= 0 && selected < stacks.size()) {
			mc.getTextureManager().bindTexture(decoration);
			drawTexturedModalRect(x-1+((selected)%gridW)*18, y-1+((selected)/gridW)*18, 1, 73, 18, 18);
		}
		
		RenderHelper.disableStandardItemLighting();
		RenderHelper.enableGUIStandardItemLighting();
		ItemStack[] stacksToDisplay = stacks.keySet().toArray(new ItemStack[] {});
		
		ItemStack toolTipStack = null;
		for (int i=0; i<Integer.min(itemsPerPage, stacksToDisplay.length-page*itemsPerPage); i++) {
			int stackDrawn = i+page*itemsPerPage;
			int xGrid = i%gridW;
			int yGrid = i/gridW;
			int ix = x+xGrid*18;
			int iy = y+yGrid*18;
			this.itemRender.renderItemIntoGUI(stacksToDisplay[i+page*itemsPerPage], ix, iy);
			this.itemRender.renderItemOverlayIntoGUI(fontRenderer, stacksToDisplay[stackDrawn], ix, iy, stacks.get(stacksToDisplay[stackDrawn]).toString());
			if (mouseX > ix && mouseX < ix+18 && mouseY > iy && mouseY < iy+18) {
				toolTipStack = stacksToDisplay[stackDrawn];
			}
		}
		RenderHelper.enableStandardItemLighting();
		
		if (toolTipStack != null) {
			this.renderToolTip(toolTipStack, mouseX, mouseY);
		}
	}
}
