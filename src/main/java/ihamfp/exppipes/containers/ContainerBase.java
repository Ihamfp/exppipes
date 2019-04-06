package ihamfp.exppipes.containers;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class ContainerBase extends Container {

	Map<Integer,Slot> playerSlots = new HashMap<Integer,Slot>();
	Map<Integer,Slot> ownSlots = new HashMap<Integer,Slot>();
	
	protected void addPlayerSlots(IInventory playerInventory, int invX, int invY) {
		for (int col=0; col<9; col++) { // player hotbar
			int x = invX + col*18;
			playerSlots.put(this.inventorySlots.size(), this.addSlotToContainer(new Slot(playerInventory, col, x, invY+58)));
		}
		
		for (int row = 0; row < 3; row++) {
			for (int col = 0; col < 9; col++) {
				int x = invX + col * 18;
				int y = invY + row * 18;
				playerSlots.put(this.inventorySlots.size(), this.addSlotToContainer(new Slot(playerInventory, col + row * 9 + 9, x, y)));
			}
		}
	}
	
	@Override
	public ItemStack transferStackInSlot(EntityPlayer playerIn, int index) {
		//ExppipesMod.logger.info("transferStackInSlot: " + Integer.toString(index));
		
        if (this.ownSlots.size() == 0 || this.playerSlots.size() == 0) return ItemStack.EMPTY;
		
		Slot slot = this.inventorySlots.get(index);
        if (slot == null || !slot.getHasStack()) return ItemStack.EMPTY;
        
        ItemStack itemStack1 = slot.getStack();
        ItemStack itemStack = itemStack1.copy();
        
        if (this.playerSlots.containsValue(slot)) { // player to container
        	if (!this.mergeItemStack(itemStack1, Collections.min(this.ownSlots.keySet()), Collections.max(this.ownSlots.keySet())+1, false)) {
        		return ItemStack.EMPTY;
        	}
        } else if (this.ownSlots.containsValue(slot)) { // container to player
        	if (!this.mergeItemStack(itemStack1, Collections.min(this.playerSlots.keySet()), Collections.max(this.playerSlots.keySet())+1, false)) {
        		return ItemStack.EMPTY;
        	}
        }
        
        if (itemStack1.isEmpty()) {
        	slot.putStack(ItemStack.EMPTY);
        } else {
        	slot.onSlotChanged();
        }
        
        return itemStack;
	}

	@Override
	public boolean canInteractWith(EntityPlayer playerIn) {
		return true;
	}
}
