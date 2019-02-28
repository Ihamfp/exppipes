package ihamfp.exppipes.containers;

import java.io.IOException;

import ihamfp.exppipes.ExppipesMod;
import ihamfp.exppipes.tileentities.pipeconfig.ConfigRoutingPipe;
import ihamfp.exppipes.tileentities.pipeconfig.FilterConfig.FilterType;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.inventory.Container;
import net.minecraft.util.ResourceLocation;

public class GuiContainerPipeConfig extends GuiContainerDecorated {
	private static final boolean showButtons = false; // set to true to test client->server sync
	
	public static final int WIDTH = 176;
	public static final int HEIGHT = 168;
	
	public static final ResourceLocation background = new ResourceLocation(ExppipesMod.MODID, "textures/gui/pipeconfig.png");
	
	public ConfigRoutingPipe conf = null;
	
	public GuiContainerPipeConfig(Container inventorySlotsIn) {
		super(inventorySlotsIn);
		if (inventorySlotsIn instanceof ContainerPipeConfig) {
			 this.conf = ((ContainerPipeConfig)inventorySlotsIn).te.sinkConfig;
		} else {
			ExppipesMod.logger.error("No associated config found - displaying empty gui");
		}
	}
	
	public GuiContainerPipeConfig(Container inventorySlotsIn, ConfigRoutingPipe conf) {
		super(inventorySlotsIn);
		this.conf = conf;
		if (conf == null) {
			ExppipesMod.logger.error("No associated config found - displaying empty gui");
		}
	}
	
	@Override
	public void initGui() {
		super.initGui();
		if (!showButtons) return;
		
		if (conf == null) return;
		for (int i=0;i<9;i++) { // config buttons
			this.addButton(new GuiButton(i*2, guiLeft+8+(i*18), guiTop+34, 18, 18, "0"));
			this.addButton(new GuiButton(i*2+1, guiLeft+8+(i*18), guiTop+52, 18, 18, "D"));
		}
		this.updateButtonText(conf);
	}
	
	public void updateButtonText(ConfigRoutingPipe conf) {
		if (!showButtons) return;
		if (conf == null) return;
		for (int i=0;i<9;i++) { // config buttons
			int priority = 0;
			String filter = "D";
			if (conf.filters.size() > i) {
				priority = conf.filters.get(i).priority;
				filter = conf.filters.get(i).filterType.getShortName();
				this.buttonList.get(i*2).visible = true;
				this.buttonList.get(i*2+1).visible = true;
			} else {
				this.buttonList.get(i*2).visible = false;
				this.buttonList.get(i*2+1).visible = false;
			}
			this.buttonList.get(i*2).displayString = Integer.toString(priority);
			this.buttonList.get(i*2+1).displayString = filter;
		}
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		mc.getTextureManager().bindTexture(background);
		drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
		this.fontRenderer.drawString("Sink Configuration", guiLeft+8, guiTop+6, 0x7f7f7f);
		this.updateButtonText(conf); // Should maybe replace it with a less resource-intensive way... But I didn't find one
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
		super.drawGuiContainerForegroundLayer(mouseX, mouseY);
		
		if (!showButtons) return;
		for (GuiButton button : this.buttonList) {
			if (button.isMouseOver() && button.enabled && button.visible) {
				if (button.id%2 == 0) { // priority
					this.drawHoveringText("Priority", mouseX, mouseY);
				} else {
					FilterType filterType = FilterType.DEFAULT;
					if (this.inventorySlots instanceof ContainerPipeConfig) {
						if (conf.filters.size() > button.id/2) {
							filterType = conf.filters.get(button.id/2).filterType;
						}
					}
					this.drawHoveringText("Filter: " + filterType.toString(), mouseX, mouseY);
				}
			}
		}
	}

	@Override
	protected void actionPerformed(GuiButton button) throws IOException {
		super.actionPerformed(button);
		if (!showButtons) return;
		
		if (conf != null) {
			if (button.id%2 == 1 && conf.filters.size() > button.id/2) { // filter type
				FilterType current = conf.filters.get(button.id/2).filterType; //FilterType.fromShortString(button.displayString);
				FilterType next = FilterType.values()[(current.ordinal()+1)%FilterType.values().length]; // cycle through
				conf.filters.get(button.id/2).filterType = next; // TODO sync to server
			}
		}
		this.updateButtonText(conf);
	}
}
