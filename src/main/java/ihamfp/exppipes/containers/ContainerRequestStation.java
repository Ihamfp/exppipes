package ihamfp.exppipes.containers;

import ihamfp.exppipes.tileentities.TileEntityRequestStation;
import net.minecraft.inventory.IInventory;
import net.minecraftforge.items.SlotItemHandler;

public class ContainerRequestStation extends ContainerTileEntity<TileEntityRequestStation> {

	public ContainerRequestStation(IInventory playerInventory, TileEntityRequestStation te) {
		super(playerInventory, te);
		this.addPlayerSlots(playerInventory, 8, 150);
		// buffer slots
		for (int i=0; i<te.inventory.getSlots(); i++) {
			int ix = i%9;
			int iy = i/9;
			
			this.ownSlots.put(this.inventorySlots.size(), this.addSlotToContainer(new SlotItemHandler(te.inventory, i, 8+ix*18, 84+iy*18)));
		}
		// crafting matrix
		for (int i=0; i<te.craftMatrix.getSlots(); i++) {
			this.ownSlots.put(this.inventorySlots.size(), this.addSlotToContainer(new SlotItemHandler(te.craftMatrix, i, 30+(i%3)*18, 17+(i/3)*18)));
		}
		// crafting result
		this.ownSlots.put(this.inventorySlots.size(), this.addSlotToContainer(new SlotItemHandler(te.craftResult, 0, 124, 35)));
		// return slot
		this.ownSlots.put(this.inventorySlots.size(), this.addSlotToContainer(new SlotItemHandler(te.returnSlot, 0, 152, 63)));
	}

}