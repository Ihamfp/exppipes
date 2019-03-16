package ihamfp.exppipes.containers;

import java.io.IOException;
import java.util.ArrayList;

import ihamfp.exppipes.ExppipesMod;
import ihamfp.exppipes.common.network.PacketHandler;
import ihamfp.exppipes.common.network.PacketInventoryRequest;
import ihamfp.exppipes.common.network.PacketItemRequest;
import ihamfp.exppipes.tileentities.InvCacheEntry;
import ihamfp.exppipes.tileentities.TileEntityRequestPipe;
import ihamfp.exppipes.tileentities.pipeconfig.FilterConfig;
import ihamfp.exppipes.tileentities.pipeconfig.FilterConfig.FilterType;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.inventory.Container;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class GuiContainerPipeRequest extends GuiContainerDecorated {
	public static final int WIDTH = 176;
	public static final int HEIGHT = 231;
	
	public static final int itemsPerPage = 54;
	
	/*
	 * true: sort by ID
	 * false: sort by count
	 */
	public static boolean sortID = false;
	
	protected int page = 0; // 1 page = 54 items 6*9
	protected int selected = -1; // -1 = nothing selected.
	
	protected TileEntityRequestPipe te;
	
	public static final ResourceLocation background = new ResourceLocation(ExppipesMod.MODID, "textures/gui/piperequest.png");
	
	public GuiContainerPipeRequest(Container inventorySlotsIn, TileEntityRequestPipe te) {
		super(inventorySlotsIn);
		this.xSize = WIDTH;
		this.ySize = HEIGHT;
		this.te = te;
	}
	
	@Override
	public void initGui() {
		if (te.invCache == null) {
			te.invCache = new ArrayList<InvCacheEntry>();
			PacketHandler.INSTANCE.sendToServer(new PacketInventoryRequest(te.getPos()));
		}
		super.initGui();
		this.addButton(new GuiButton(0, guiLeft+106, guiTop+128, 64, 18, "Request"));
		this.addButton(new GuiButton(1, guiLeft+6, guiTop+128, 64, 18, "Refresh"));
		this.addButton(new GuiButton(2, guiLeft+164, guiTop+4, 6, 11, ">")); // next page
		this.addButton(new GuiButton(3, guiLeft+136, guiTop+4, 6, 11, "<")); // previous page
		this.addButton(new GuiButton(4, guiLeft+73, guiTop+128, 30, 18, "Sort"));
	}
	
	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		super.mouseClicked(mouseX, mouseY, mouseButton);
		if (mouseX > guiLeft+6 && mouseY > guiTop+16 && mouseX < guiLeft+169 && mouseY < guiTop+125) { // item selection box
			int clickX = mouseX-6-guiLeft;
			int clickY = mouseY-16-guiTop;
			int gridX = clickX/18;
			int gridY = clickY/18;
			this.selected = gridX + gridY*9;
			
			if (te.invCache.size() <= this.selected + this.page*itemsPerPage) this.selected = -1;
		}
	}

	public ItemStack getSelectedItem() {
		if (selected < 0) return null;
		return te.invCache.get(selected + page*itemsPerPage).stack;
	}
	
	@Override
	protected void actionPerformed(GuiButton button) throws IOException {
		super.actionPerformed(button);
		if (button.id == 0 && selected >= 0) { // request
			InvCacheEntry entry = te.invCache.get(selected + page*itemsPerPage);
			PacketHandler.INSTANCE.sendToServer(new PacketItemRequest(te.getPos(), new FilterConfig(entry.stack, FilterType.STRICT)));
			
			entry.count--;
			if (entry.count == 0) {
				this.te.invCache.remove(entry);
			}
		} else if (button.id == 1) {
			this.selected = -1;
			this.te.invCache.clear();
			PacketHandler.INSTANCE.sendToServer(new PacketInventoryRequest(te.getPos()));
		} else if (button.id == 2) {
			this.page++;
			if (this.page > this.te.invCache.size()/itemsPerPage) {
				this.page = this.te.invCache.size()/itemsPerPage;
			}
		} else if (button.id == 3) {
			this.page--;
			if (this.page < 0) {
				this.page = 0;
			}
		} else if (button.id == 4) {
			sortID = !sortID;
			if (sortID) { // sort by id
				te.invCache.sort((a,b) -> Item.getIdFromItem(a.stack.getItem()) - Item.getIdFromItem(b.stack.getItem()));
			} else {
				te.invCache.sort((a,b) -> b.count - a.count); // reverse count
			}
		}
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		mc.getTextureManager().bindTexture(background);
		drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
		this.fontRenderer.drawString("Request pipe", guiLeft+8, guiTop+6, 0x7f7f7f);
		this.fontRenderer.drawString(Integer.toString(this.page+1) + "/" + Integer.toString(1+this.te.invCache.size()/itemsPerPage), guiLeft+143, guiTop+7, 0x7f7f7f);
		
		if (te.invCache == null) return;
		
		this.drawItemSelector(guiLeft+8, guiTop+18, 9, 6, this.te.invCache, this.selected, this.page, mouseX, mouseY);
		if (this.buttonList.get(4).isMouseOver()) { // sorting by id/count
			this.drawHoveringText("Sorting by " + (sortID?"ID":"count"), mouseX, mouseY);
		}
	}
}
