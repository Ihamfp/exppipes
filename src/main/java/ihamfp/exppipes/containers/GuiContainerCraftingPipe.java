package ihamfp.exppipes.containers;

import ihamfp.exppipes.ExppipesMod;
import net.minecraft.inventory.Container;
import net.minecraft.util.ResourceLocation;

public class GuiContainerCraftingPipe extends GuiContainerDecorated {
	public static final int WIDTH = 176;
	public static final int HEIGHT = 168;
	
	public static final ResourceLocation background = new ResourceLocation(ExppipesMod.MODID, "textures/gui/pipeconfig.png");

	public GuiContainerCraftingPipe(Container inventorySlotsIn) {
		super(inventorySlotsIn);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		mc.getTextureManager().bindTexture(background);
		drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
		this.fontRenderer.drawString("Crafting patterns", guiLeft+8, guiTop+6, 0x7f7f7f);
	}

}
