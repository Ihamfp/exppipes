package ihamfp.exppipes.containers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ihamfp.exppipes.ExppipesMod;
import ihamfp.exppipes.common.Configs;
import ihamfp.exppipes.common.network.PacketFilterChange;
import ihamfp.exppipes.common.network.PacketFilterChange.FilterFunction;
import ihamfp.exppipes.common.network.PacketHandler;
import ihamfp.exppipes.common.network.PacketSetDefaultRoute;
import ihamfp.exppipes.tileentities.TileEntityRoutingPipe;
import ihamfp.exppipes.tileentities.TileEntitySupplierPipe;
import ihamfp.exppipes.tileentities.pipeconfig.ConfigRoutingPipe;
import ihamfp.exppipes.tileentities.pipeconfig.Filters;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.inventory.Container;
import net.minecraft.util.ResourceLocation;

public class GuiContainerPipeConfig extends GuiContainerDecorated {
	public static final int WIDTH = 176;
	public static final int HEIGHT = 168;
	
	public static final ResourceLocation background = new ResourceLocation(ExppipesMod.MODID, "textures/gui/pipeconfig.png");
	
	public TileEntityRoutingPipe te = null;
	public ConfigRoutingPipe conf = null;
	public FilterFunction filterFunction = FilterFunction.FILTER_SINK;
	public String confTitle = "Pipe configuration";
	
	public GuiContainerPipeConfig(Container inventorySlotsIn, TileEntityRoutingPipe te, FilterFunction filterFunction) {
		super(inventorySlotsIn);
		this.te = te;
		this.conf = te.sinkConfig;
		if (te instanceof TileEntitySupplierPipe && filterFunction == FilterFunction.FILTER_SUPPLY) {
			this.conf = ((TileEntitySupplierPipe)te).supplyConfig;
		}
		this.filterFunction = filterFunction;
		this.confTitle = (filterFunction == FilterFunction.FILTER_SUPPLY)?"Supply config":"Sink config";
		if (conf == null) {
			ExppipesMod.logger.error("No associated config found - displaying empty gui");
		}
	}
	
	@Override
	public void initGui() {
		super.initGui();
		
		if (conf == null) return;
		for (int i=0;i<9;i++) { // config buttons
			this.addButton(new GuiButton(i*2, guiLeft+7+(i*18), guiTop+35, 18, 18, "0"));
			this.addButton(new GuiButton(i*2+1, guiLeft+7+(i*18), guiTop+53, 18, 18, "D"));
		}
		if (this.filterFunction == FilterFunction.FILTER_SINK) {
			this.addButton(new GuiButton(18, guiLeft+105, guiTop+4, 64, 11, "Default sink"));
		}
		this.updateButtonText(conf);
	}
	
	public void updateButtonText(ConfigRoutingPipe conf) {
		if (conf == null) return;
		for (int i=0;i<9;i++) { // config buttons
			int priority = 0;
			String filter = "D";
			boolean blacklist = false;
			if (conf.filters.size() > i) {
				priority = conf.filters.get(i).priority;
				filter = Filters.filters.get(conf.filters.get(i).filterId).getShortName();
				blacklist = conf.filters.get(i).blacklist;
				this.buttonList.get(i*2).visible = true;
				this.buttonList.get(i*2+1).visible = true;
			} else {
				this.buttonList.get(i*2).visible = false;
				this.buttonList.get(i*2+1).visible = false;
			}
			this.buttonList.get(i*2).displayString = Integer.toString(priority);
			this.buttonList.get(i*2+1).packedFGColour = blacklist?0xff0000:0;
			this.buttonList.get(i*2+1).displayString = filter;
		}
		
		if (this.filterFunction == FilterFunction.FILTER_SINK) {
			if (this.te.isDefaultRoute) {
				this.buttonList.get(18).enabled = false;
			} else {
				this.buttonList.get(18).enabled = true;
			}
		}
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		mc.getTextureManager().bindTexture(background);
		drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
		this.fontRenderer.drawString(this.confTitle, guiLeft+8, guiTop+6, 0x7f7f7f);
		this.updateButtonText(conf); // Should maybe replace it with a less resource-intensive way... But I didn't find one
	}
	
	@SuppressWarnings("serial")
	static List<String> filterHoverText = new ArrayList<String>() {{
		add("Filter: ");
		if (Configs.showHelpTooltips) add("(Shift+click to toggle blacklist)");
	}};
	
	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
		super.drawGuiContainerForegroundLayer(mouseX, mouseY);
		
		for (GuiButton button : this.buttonList) {
			if (button.isMouseOver() && button.enabled && button.visible && conf != null && button.id/2 < conf.filters.size()) {
				if (button.id%2 == 0) { // priority
					this.drawHoveringText("Priority", mouseX-guiLeft, mouseY-guiTop);
				} else {
					int filterId = 0;
					boolean blacklist = false;
					if (this.inventorySlots instanceof ContainerPipeConfig) {
						if (conf.filters.size() > button.id/2) {
							filterId = conf.filters.get(button.id/2).filterId;
							blacklist = conf.filters.get(button.id/2).blacklist;
						}
					}
					filterHoverText.set(0, "Filter: " + Filters.filters.get(filterId).getLongName() + (blacklist?" blacklist":""));
					this.drawHoveringText(filterHoverText, mouseX-guiLeft, mouseY-guiTop);
				}
			}
		}
	}

	@Override
	protected void actionPerformed(GuiButton button) throws IOException {
		super.actionPerformed(button);
		
		if (conf != null) {
			if (button.id%2 == 1 && conf.filters.size() > button.id/2) { // filter type
				if (isShiftKeyDown()) {
					conf.filters.get(button.id/2).blacklist = !conf.filters.get(button.id/2).blacklist;
				} else {
					conf.filters.get(button.id/2).filterId = (conf.filters.get(button.id/2).filterId+1)%Filters.filters.size();
				}
			} else if (button.id%2 == 0 && conf.filters.size() > button.id/2) { // priority
				conf.filters.get(button.id/2).priority += ((isShiftKeyDown())?-1:+1)*(isCtrlKeyDown()?10:1);
			}
			if (conf.filters.size() > button.id/2 && ((ContainerPipeConfig)inventorySlots).te != null) {
				PacketHandler.INSTANCE.sendToServer(new PacketFilterChange(this.te.getPos(), button.id/2, conf.filters.get(button.id/2).filterId, conf.filters.get(button.id/2).blacklist, conf.filters.get(button.id/2).priority, this.filterFunction));
			}
		}
		if (this.filterFunction == FilterFunction.FILTER_SINK && button.id == 18) { // set default route
			this.te.isDefaultRoute = true;
			PacketHandler.INSTANCE.sendToServer(new PacketSetDefaultRoute(this.te.getPos()));
		}
		this.updateButtonText(conf);
	}
}
