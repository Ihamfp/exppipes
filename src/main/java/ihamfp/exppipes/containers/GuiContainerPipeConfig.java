package ihamfp.exppipes.containers;

import java.io.IOException;

import ihamfp.exppipes.ExppipesMod;
import ihamfp.exppipes.common.network.PacketFilterChange;
import ihamfp.exppipes.common.network.PacketFilterChange.FilterFunction;
import ihamfp.exppipes.common.network.PacketHandler;
import ihamfp.exppipes.tileentities.pipeconfig.ConfigRoutingPipe;
import ihamfp.exppipes.tileentities.pipeconfig.FilterConfig.FilterType;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.inventory.Container;
import net.minecraft.util.ResourceLocation;

public class GuiContainerPipeConfig extends GuiContainerDecorated {
	public static final int WIDTH = 176;
	public static final int HEIGHT = 168;
	
	public static final ResourceLocation background = new ResourceLocation(ExppipesMod.MODID, "textures/gui/pipeconfig.png");
	
	public ConfigRoutingPipe conf = null;
	public String confTitle = "Pipe configuration";
	
	public GuiContainerPipeConfig(Container inventorySlotsIn) {
		super(inventorySlotsIn);
		if (inventorySlotsIn instanceof ContainerPipeConfig) {
			 this.conf = ((ContainerPipeConfig)inventorySlotsIn).te.sinkConfig;
			 this.confTitle = "Sink configuration";
		} else {
			ExppipesMod.logger.error("No associated config found - displaying empty gui");
		}
	}
	
	public GuiContainerPipeConfig(Container inventorySlotsIn, ConfigRoutingPipe conf, String confTitle) {
		super(inventorySlotsIn);
		this.conf = conf;
		this.confTitle = confTitle;
		if (conf == null) {
			ExppipesMod.logger.error("No associated config found - displaying empty gui");
		}
	}
	
	@Override
	public void initGui() {
		super.initGui();
		
		if (conf == null) return;
		for (int i=0;i<9;i++) { // config buttons
			this.addButton(new GuiButton(i*2, guiLeft+8+(i*18), guiTop+34, 18, 18, "0"));
			this.addButton(new GuiButton(i*2+1, guiLeft+8+(i*18), guiTop+52, 18, 18, "D"));
		}
		this.updateButtonText(conf);
	}
	
	public void updateButtonText(ConfigRoutingPipe conf) {
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
		this.fontRenderer.drawString(this.confTitle, guiLeft+8, guiTop+6, 0x7f7f7f);
		this.updateButtonText(conf); // Should maybe replace it with a less resource-intensive way... But I didn't find one
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
		super.drawGuiContainerForegroundLayer(mouseX, mouseY);
		
		for (GuiButton button : this.buttonList) {
			if (button.isMouseOver() && button.enabled && button.visible) {
				if (button.id%2 == 0) { // priority
					this.drawHoveringText("Priority", mouseX-guiLeft, mouseY-guiTop);
				} else {
					FilterType filterType = FilterType.DEFAULT;
					if (this.inventorySlots instanceof ContainerPipeConfig) {
						if (conf.filters.size() > button.id/2) {
							filterType = conf.filters.get(button.id/2).filterType;
						}
					}
					this.drawHoveringText("Filter: " + filterType.toString(), mouseX-guiLeft, mouseY-guiTop);
				}
			}
		}
	}

	@Override
	protected void actionPerformed(GuiButton button) throws IOException {
		super.actionPerformed(button);
		
		if (conf != null) {
			if (button.id%2 == 1 && conf.filters.size() > button.id/2) { // filter type
				FilterType current = conf.filters.get(button.id/2).filterType; //FilterType.fromShortString(button.displayString);
				FilterType next = FilterType.values()[(current.ordinal()+1)%FilterType.values().length]; // cycle through
				conf.filters.get(button.id/2).filterType = next;
			}
			if (((ContainerPipeConfig)inventorySlots).te != null) {
				PacketHandler.INSTANCE.sendToServer(new PacketFilterChange(((ContainerPipeConfig)inventorySlots).te.getPos(), button.id/2, conf.filters.get(button.id/2).priority, this.confTitle.equals("Sink configuration")?FilterFunction.FILTER_SINK:FilterFunction.FILTER_SUPPLY)); // TODO clean this shit
			}
		}
		this.updateButtonText(conf);
	}
}
