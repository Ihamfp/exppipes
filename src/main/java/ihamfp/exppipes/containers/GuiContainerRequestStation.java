package ihamfp.exppipes.containers;

import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.input.Mouse;

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
	static boolean reqOre = false;
	
	boolean searchBarSelected = false;
	String searchBar = "";
	String oldSearchBar = "";
	List<InvCacheEntry> searchItems = null;
	
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
		this.addButton(new GuiButton(7, guiLeft+174, guiTop+203, 48, 10, "OreDict"));
		this.buttonList.get(7).packedFGColour = reqOre?0:0xff0000;
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
		if (mouseX > guiLeft+174 && mouseY > guiTop+9 && mouseX < guiLeft+296 && mouseY < guiTop+21) {
			this.searchBarSelected = true;
		} else {
			this.searchBarSelected = false;
		}
	}
	
	@Override
	public void handleMouseInput() throws IOException {
		int d = Mouse.getEventDWheel();
		if (d != 0) {
			this.reqCount += (d<0?-1:1)*(isShiftKeyDown()?64:1)*(isCtrlKeyDown()?16:1);
			if (reqCount < 1) reqCount = 1;
		}
		super.handleMouseInput();
	}
	
	@Override
	protected void keyTyped(char typedChar, int keyCode) throws IOException {
		if (this.searchBarSelected) {
			switch (keyCode) {
			case 1:
				this.mc.player.closeScreen();
				return;
			case 14:
				if (this.searchBar.length() > 0) this.searchBar = this.searchBar.substring(0, this.searchBar.length()-1);
				return;
			}
			// check if the character is printable
			Character.UnicodeBlock block = Character.UnicodeBlock.of(typedChar);
			if (!Character.isISOControl(typedChar) && typedChar != KeyEvent.CHAR_UNDEFINED && block != null && block != Character.UnicodeBlock.SPECIALS)
				this.searchBar += typedChar;
		} else {
			super.keyTyped(typedChar, keyCode);
		}
	}
	
	@Override
	protected void actionPerformed(GuiButton button) throws IOException {
		super.actionPerformed(button);
		if (button.id == 0 && selected >= 0) { // request
			InvCacheEntry entry = (this.searchItems==null?this.te.invCache:this.searchItems).get(selected + page*itemsPerPage);
			// search for a connected routing pipe
			TileEntityRoutingPipe terp = null;
			for (EnumFacing f : EnumFacing.VALUES) {
				TileEntity teCheck = te.getWorld().getTileEntity(te.getPos().offset(f));
				if (teCheck instanceof TileEntityRoutingPipe) {
					terp = (TileEntityRoutingPipe) teCheck;
					break;
				}
			}
			if (terp != null) {
				PacketHandler.INSTANCE.sendToServer(new PacketItemRequest(new BlockDimPos(terp), new FilterConfig(entry.stack, reqOre?4:2, false), this.reqCount));
			} else {
				ExppipesMod.logger.info("Requested things from station with no network");
			}
			
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
			if (this.page > (this.searchItems==null?this.te.invCache:this.searchItems).size()/itemsPerPage) {
				this.page = (this.searchItems==null?this.te.invCache:this.searchItems).size()/itemsPerPage;
			}
		} else if (button.id == 3) { // page <
			this.page--;
			if (this.page < 0) this.page = 0;
		} else if (button.id == 4) { // sort
			Utils.sortID = !Utils.sortID;
			Utils.invCacheSort(te.invCache);
			if (this.searchItems!=null) Utils.invCacheSort(this.searchItems);
		} else if (button.id == 5 || button.id == 6) { // +/-
			int addCount = (button.id==5?-1:1)*(isShiftKeyDown()?64:1)*(isCtrlKeyDown()?16:1) - (this.reqCount==1?1:0);
			this.reqCount += addCount;
			if (reqCount < 1) reqCount = 1;
		} else if (button.id == 7) { // oredict
			reqOre = !reqOre;
			button.packedFGColour = reqOre?0:0xff0000;
		}
	}
	
	public void updateButtonText() {
		// +/- buttons
		int charmult = (isShiftKeyDown()?3:1)+(isCtrlKeyDown()?1:0);
		this.buttonList.get(5).displayString = new String(new char[charmult]).replace("\0", "-");
		this.buttonList.get(6).displayString = new String(new char[charmult]).replace("\0", "+");
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		this.updateButtonText();
		mc.getTextureManager().bindTexture(background);
		drawModalRectWithCustomSizedTexture(guiLeft, guiTop, 0.0f, 0.0f, WIDTH, HEIGHT, 512, 256);
		// page count
		String pageString = Integer.toString(this.page+1) + "/" + Integer.toString(1+this.te.invCache.size()/itemsPerPage);
		this.fontRenderer.drawString(pageString, guiLeft+318-(this.fontRenderer.getStringWidth(pageString)/2), guiTop+11, 0x7f7f7f);
		// request count
		int stackSize = 64;
		if (this.te == null || this.te.invCache == null || selected >= this.te.invCache.size()) selected = -1;
		if (selected >= 0) {
			stackSize = this.te.invCache.get(this.selected + this.page*itemsPerPage).stack.getMaxStackSize();
		}
		String countString = "";
		if (stackSize == 1) {
			countString = Integer.toString(this.reqCount);
		} else {
			if (this.reqCount/stackSize > 0) countString += Integer.toString(this.reqCount/stackSize) + "s+";
			countString += Integer.toString(this.reqCount%stackSize);
		}
		// page
		this.fontRenderer.drawString(countString, guiLeft+257-(this.fontRenderer.getStringWidth(countString)/2), guiTop+215, 0x7f7f7f);
		// search
		if (this.fontRenderer.getStringWidth(this.searchBar + (this.searchBarSelected?"_":" ")) <= 120) {
			this.fontRenderer.drawString(this.searchBar + (this.searchBarSelected?"_":" "), guiLeft+176, guiTop+11, 0xffffffff);
		} else {
			String subString = this.searchBar;
			while (this.fontRenderer.getStringWidth(subString + (this.searchBarSelected?"_":" ")) > 120) {
				subString = subString.substring(1); // remove 1 character at the beginning
			}
			this.fontRenderer.drawString(subString + (this.searchBarSelected?"_":" "), guiLeft+176, guiTop+11, 0xffffffff);
		}
	}
	
	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
		if (this.te.invCache != null) {
			if (!this.oldSearchBar.equals(this.searchBar)) {
				this.oldSearchBar = this.searchBar;
				if (this.searchBar.equals("")) this.searchItems = null;
				else this.searchItems = Utils.invCacheSearch(this.te.invCache, searchBar);
			}
			this.drawItemSelector(176, 24, 9, 9, this.searchItems==null?this.te.invCache:this.searchItems, this.selected, this.page, mouseX, mouseY);
		}
		super.drawGuiContainerForegroundLayer(mouseX, mouseY);
	}
}
