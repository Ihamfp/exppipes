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
import ihamfp.exppipes.pipenetwork.BlockDimPos;
import ihamfp.exppipes.tileentities.TileEntityExtractionPipe;
import ihamfp.exppipes.tileentities.TileEntityRoutingPipe;
import ihamfp.exppipes.tileentities.TileEntityStockKeeperPipe;
import ihamfp.exppipes.tileentities.TileEntitySupplierPipe;
import ihamfp.exppipes.tileentities.pipeconfig.ConfigRoutingPipe;
import ihamfp.exppipes.tileentities.pipeconfig.FilterConfig;
import ihamfp.exppipes.tileentities.pipeconfig.Filters;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;

public class GuiContainerPipeConfig extends GuiContainerDecorated {
	public static final int WIDTH = 176;
	public static final int HEIGHT = 168;
	
	public static final ResourceLocation background = new ResourceLocation(ExppipesMod.MODID, "textures/gui/pipeconfig.png");
	
	public TileEntityRoutingPipe te = null;
	public ConfigRoutingPipe conf = null;
	public FilterFunction filterFunction = null;
	public String confTitle = "Pipe configuration";
	
	public GuiContainerPipeConfig(Container inventorySlotsIn, TileEntityRoutingPipe te, FilterFunction filterFunction) {
		super(inventorySlotsIn);
		this.te = te;
		this.conf = te.sinkConfig;
		if (te instanceof TileEntitySupplierPipe && filterFunction == FilterFunction.FILTER_SUPPLY) {
			this.conf = ((TileEntitySupplierPipe)te).supplyConfig;
			this.confTitle = "Supply config";
		} else if (te instanceof TileEntityExtractionPipe && filterFunction == FilterFunction.FILTER_EXTRACT) {
			this.conf = ((TileEntityExtractionPipe)te).extractConfig;
			this.confTitle = "Extract config";
		} else if (te instanceof TileEntityStockKeeperPipe && filterFunction == FilterFunction.FILTER_STOCK) {
			this.conf = ((TileEntityStockKeeperPipe)te).stockConfig;
			this.confTitle = "Stock config";
		}
		this.filterFunction = filterFunction;
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
		if (this.te.upgradesItemHandler != null) {
			drawTexturedModalRect(guiLeft-44, guiTop, 180, 0, 47, 60);
		}
		drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
		this.fontRenderer.drawString(this.confTitle, guiLeft+8, guiTop+6, 0x7f7f7f);
		this.updateButtonText(conf); // Should maybe replace it with a less resource-intensive way... But I didn't find one
	}
	
	@SuppressWarnings("serial")
	static List<String> filterHoverText = new ArrayList<String>() {{
		add("Filter: ");
		if (Configs.showHelpTooltips) add(TextFormatting.DARK_GRAY + "(Shift+click to toggle blacklist)");
	}};
	
	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
		super.drawGuiContainerForegroundLayer(mouseX, mouseY);
		
		for (GuiButton button : this.buttonList) { // hover text
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
		
		List<String> hoverText = null;
		RenderHelper.disableStandardItemLighting();
		RenderHelper.enableGUIStandardItemLighting();
		for (int i=0; i<9; i++) { // filters items
			int ix = 8+(i*18);
			ItemStack stackToDraw = null;
			if (i<conf.filters.size()) {
				stackToDraw = conf.filters.get(i).reference;
				this.itemRender.renderItemIntoGUI(stackToDraw, ix, 18);
				this.itemRender.renderItemOverlayIntoGUI(fontRenderer, stackToDraw, ix, 18, stackToDraw.getCount()==1?"":Integer.toString(stackToDraw.getCount()));
			}
			if (mouseX-guiLeft >= ix && mouseX-guiLeft <= ix+16 && mouseY-guiTop >= 18 && mouseY-guiTop <= 18+16) {
				drawRect(ix, 18, ix + 16, 18 + 16, -2130706433);
				if (stackToDraw != null && !stackToDraw.isEmpty()) {
					hoverText = this.getItemToolTip(stackToDraw);
				}
			}
		}
		RenderHelper.enableStandardItemLighting();
		if (hoverText != null) {
			this.drawHoveringText(hoverText, mouseX-this.guiLeft, mouseY-this.guiTop);
		}
	}

	@Override
	protected void actionPerformed(GuiButton button) throws IOException {
		super.actionPerformed(button);
		if (this.filterFunction == FilterFunction.FILTER_SINK && button.id == 18) { // set default route
			this.te.isDefaultRoute = true;
			PacketHandler.INSTANCE.sendToServer(new PacketSetDefaultRoute(this.te.getPos()));
		}
		this.updateButtonText(conf);
	}
	
	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		mouseX -= this.guiLeft;
		mouseY -= this.guiTop;
		
		// check buttons
		int button = -1;
		for (GuiButton butt : this.buttonList) {
			if (butt.isMouseOver()) button = butt.id;
		}
		if (conf != null && button >= 0 && button/2 < this.conf.filters.size()) { // config button
			if (button%2 == 1 && conf.filters.size() > button/2) { // filter type
				FilterConfig clickedFilter = conf.filters.get(button/2);
				if (isShiftKeyDown()) {
					clickedFilter.blacklist = !clickedFilter.blacklist;
				} else {
					do {
						if (mouseButton == 1) { // right click, decrement
							clickedFilter.filterId = (clickedFilter.filterId==0)?Filters.filters.size()-1:(clickedFilter.filterId-1)%Filters.filters.size();
						} else { // anything else, increment
							clickedFilter.filterId = (clickedFilter.filterId+1)%Filters.filters.size();
						}
					} while (!Filters.filters.get(clickedFilter.filterId).willEverMatch(clickedFilter.reference));
				}
			} else if (button%2 == 0 && conf.filters.size() > button/2) { // priority
				conf.filters.get(button/2).priority += ((mouseButton==1)?-1:+1)*(isShiftKeyDown()?10:1)*(isCtrlKeyDown()?100:1);
			}
			if (conf.filters.size() > button/2 && ((ContainerPipeConfig)inventorySlots).te != null) {
				PacketHandler.INSTANCE.sendToServer(new PacketFilterChange(new BlockDimPos(this.te), button/2, conf.filters.get(button/2).filterId, conf.filters.get(button/2).blacklist, conf.filters.get(button/2).priority, conf.filters.get(button/2).reference, this.filterFunction));
			}
		}
		
		// check filter slots
		if (mouseX >= 8 && mouseX <=168 && mouseY >= 18 && mouseY <= 34) {
			int clickedSlot = (mouseX-8)/18;
			ItemStack heldStack = this.mc.player.inventory.getItemStack();
			if (clickedSlot < this.conf.filters.size()) { // existing filters
				if (heldStack.isEmpty()) {
					if (isShiftKeyDown()) this.conf.filters.get(clickedSlot).reference = ItemStack.EMPTY;
					else this.conf.filters.get(clickedSlot).reference.shrink(1);
				} else if (ItemStack.areItemsEqual(this.conf.filters.get(clickedSlot).reference, heldStack)) {
					this.conf.filters.get(clickedSlot).reference.grow(mouseButton==1?1:heldStack.getCount());
				} else {
					this.conf.filters.get(clickedSlot).reference = heldStack.copy();
					if (mouseButton == 1) this.conf.filters.get(clickedSlot).reference.setCount(1);
				}
				PacketHandler.INSTANCE.sendToServer(new PacketFilterChange(new BlockDimPos(this.te), clickedSlot, conf.filters.get(clickedSlot).filterId, conf.filters.get(clickedSlot).blacklist, conf.filters.get(clickedSlot).priority, conf.filters.get(clickedSlot).reference, this.filterFunction));
				if (this.conf.filters.get(clickedSlot).reference.isEmpty()) {
					this.conf.filters.remove(clickedSlot);
				}
			} else if (!heldStack.isEmpty() && this.conf.filters.size()<9) { // add filter
				this.conf.filters.add(new FilterConfig(heldStack.copy(), 0, false));
				clickedSlot = this.conf.filters.size()-1;
				if (mouseButton == 1) this.conf.filters.get(clickedSlot).reference.setCount(1);
				PacketHandler.INSTANCE.sendToServer(new PacketFilterChange(new BlockDimPos(this.te), clickedSlot, conf.filters.get(clickedSlot).filterId, conf.filters.get(clickedSlot).blacklist, conf.filters.get(clickedSlot).priority, conf.filters.get(clickedSlot).reference, this.filterFunction));
			}
		}
		
		mouseX += this.guiLeft;
		mouseY += this.guiTop;
		super.mouseClicked(mouseX, mouseY, mouseButton);
	}
}
