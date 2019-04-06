package ihamfp.exppipes.containers;

import ihamfp.exppipes.tileentities.TileEntityCraftingPipe;
import net.minecraft.inventory.IInventory;
import net.minecraftforge.items.SlotItemHandler;

public class ContainerCraftingPipe extends ContainerTileEntity<TileEntityCraftingPipe> {

	public ContainerCraftingPipe(IInventory playerInventory, TileEntityCraftingPipe te) {
		super(playerInventory, te);
		this.addPlayerSlots(playerInventory, 8, 86);
		this.addOwnSlots(te);
		
	}
	
	protected void addOwnSlots(TileEntityCraftingPipe te) {
		for (int i=0; i<9;i++) {
			this.ownSlots.put(this.inventorySlots.size(), this.addSlotToContainer(new SlotItemHandler(te.patternStorage, i, 8+i*18, 18)));
		}
	}

}
