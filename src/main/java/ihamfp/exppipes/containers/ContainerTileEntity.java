package ihamfp.exppipes.containers;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.tileentity.TileEntity;

public abstract class ContainerTileEntity<T extends TileEntity> extends ContainerBase {
	protected T te;
	
	public ContainerTileEntity(IInventory playerInventory, T te) {
		this.te = te;
		//this.addOwnSlots();
	}

	@Override
	public boolean canInteractWith(EntityPlayer playerIn) {
		return !this.te.isInvalid() && playerIn.getDistanceSqToCenter(this.te.getPos()) < 64D;
	}

}
