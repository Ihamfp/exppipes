package ihamfp.exppipes.containers;

import java.io.IOException;
import java.util.ArrayList;

import ihamfp.exppipes.ExppipesMod;
import ihamfp.exppipes.Utils;
import ihamfp.exppipes.common.network.PacketHandler;
import ihamfp.exppipes.common.network.PacketInventoryRequest;
import ihamfp.exppipes.common.network.PacketItemRequest;
import ihamfp.exppipes.pipenetwork.BlockDimPos;
import ihamfp.exppipes.tileentities.InvCacheEntry;
import ihamfp.exppipes.tileentities.TileEntityRequestStation;
import ihamfp.exppipes.tileentities.TileEntityRoutingPipe;
import ihamfp.exppipes.tileentities.pipeconfig.FilterConfig;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.inventory.Container;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;

public class GuiContainerRequestStation extends GuiContainerDecorated {
	public static final int WIDTH = 344;
	public static final int HEIGHT = 232;
	
	public static final int itemsPerPage = 81;
	
	public static final ResourceLocation background = new ResourceLocation(ExppipesMod.MODID, "textures/gui/requeststation.png");
	
	protected TileEntityRequestStation te;
	
	int selected = -1;
	int page = 0;
	int reqCount = 1;

	public GuiContainerRequestStation(Container inventorySlotsIn, TileEntityRequestStation te) {
		super(inventorySlotsIn);
		this.xSize = WIDTH;
		this.ySize = HEIGHT;
		this.te = te;
	}
	
	@Override
	public void initGui() {
		if (te.invCache == null) {
			te.invCache = new ArrayList<InvCacheEntry>();
			PacketHandler.INSTANCE.sendToServer(new PacketInventoryRequest(new BlockDimPos(te)));
		}
		super.initGui();
		
		this.addButton(new GuiButton(0, guiLeft+224, guiTop+187, 64, 15, "Request"));
		this.addButton(new GuiButton(1, guiLeft+174, guiTop+187, 48, 15, "Refresh"));
		this.addButton(new GuiButton(2, guiLeft+332, guiTop+9, 6, 11, ">")); // next page
		this.addButton(new GuiButton(3, guiLeft+298, guiTop+9, 6, 11, "<")); // previous page
		this.addButton(new GuiButton(4, guiLeft+290, guiTop+187, 48, 15, "Sort"));
		this.addButton(new GuiButton(5, guiLeft+224, guiTop+203, 31, 10, "-")); // decrease request size
		this.addButton(new GuiButton(6, guiLeft+258, guiTop+203, 31, 10, "+")); // increase request size
	}
	
	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		super.mouseClicked(mouseX, mouseY, mouseButton);
		if (mouseX > guiLeft+176 && mouseY > guiTop+24 && mouseX < guiLeft+338 && mouseY < guiTop+186) { // item selection box
			int clickX = mouseX-176-guiLeft;
			int clickY = mouseY-24-guiTop;
			int gridX = clickX/18;
			int gridY = clickY/18;
			this.selected = gridX + gridY*9;
			
			if (te.invCache.size() <= this.selected + this.page*itemsPerPage) this.selected = -1;
		}
	}
	
	@Override
	protected void actionPerformed(GuiButton button) throws IOException {
		super.actionPerformed(button);
		if (button.id == 0 && selected >= 0) { // request
			InvCacheEntry entry = te.invCache.get(selected + page*itemsPerPage);
			// search for a connected routing pipe
			TileEntityRoutingPipe terp = null;
			for (EnumFacing f : EnumFacing.VALUES) {
				TileEntity teCheck = te.getWorld().getTileEntity(te.getPos().offset(f));
				if (teCheck instanceof TileEntityRoutingPipe) {
					terp = (TileEntityRoutingPipe) teCheck;
					break;
				}
			}
			PacketHandler.INSTANCE.sendToServer(new PacketItemRequest(new BlockDimPos(terp), new FilterConfig(entry.stack, 2, false), this.reqCount));
			
			if (entry.count != 0) {
				entry.count -= this.reqCount; // do some client-side prediction
				if (entry.count <= 0) {
					if (entry.craftable) entry.count = 0;
					else this.te.invCache.remove(entry);
				}
			}
		} else if (button.id == 1) { // refresh
			//this.selected = -1;
			//this.te.invCache.clear();
			PacketHandler.INSTANCE.sendToServer(new PacketInventoryRequest(new BlockDimPos(te)));
		} else if (button.id == 2) { // page >
			this.page++;
			if (this.page > this.te.invCache.size()/itemsPerPage) {
				this.page = this.te.invCache.size()/itemsPerPage;
			}
		} else if (button.id == 3) { // page <
			this.page--;
			if (this.page < 0) this.page = 0;
		} else if (button.id == 4) { // sort
			Utils.sortID = !Utils.sortID;
			Utils.invCacheSort(te.invCache);
		} else if (button.id == 5) { // -
			this.reqCount -= 1;
			if (reqCount < 1) reqCount = 1;
		} else if (button.id == 6) { // +
			this.reqCount += 1;
		}
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		mc.getTextureManager().bindTexture(background);
		drawModalRectWithCustomSizedTexture(guiLeft, guiTop, 0.0f, 0.0f, WIDTH, HEIGHT, 512, 256);
		// page count
		String pageString = Integer.toString(this.page+1) + "/" + Integer.toString(1+this.te.invCache.size()/itemsPerPage);
		this.fontRenderer.drawString(pageString, guiLeft+318-(this.fontRenderer.getStringWidth(pageString)/2), guiTop+11, 0x7f7f7f);
		// request count
		String countString = Integer.toString(this.reqCount);
		this.fontRenderer.drawString(countString, guiLeft+257-(this.fontRenderer.getStringWidth(countString)/2), guiTop+215, 0x7f7f7f);
	}
	
	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
		if (this.te.invCache != null) {
			this.drawItemSelector(176, 24, 9, 9, this.te.invCache, this.selected, this.page, mouseX, mouseY);
		}
		super.drawGuiContainerForegroundLayer(mouseX, mouseY);
	}

}
