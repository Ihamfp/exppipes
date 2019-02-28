package ihamfp.exppipes.containers;

import ihamfp.exppipes.tileentities.TileEntityRequestPipe;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

public class ContainerRequest extends ContainerBase<TileEntityRequestPipe> {
	public ContainerRequest(IInventory playerInventory, TileEntityRequestPipe te) {
		super(playerInventory, te);
	}
	
	@Override
	public ItemStack transferStackInSlot(EntityPlayer playerIn, int index) {
		return ItemStack.EMPTY;
	}
}
