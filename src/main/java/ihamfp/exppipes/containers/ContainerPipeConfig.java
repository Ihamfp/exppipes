package ihamfp.exppipes.containers;

import ihamfp.exppipes.ExppipesMod;
import ihamfp.exppipes.items.ItemUpgrade;
import ihamfp.exppipes.tileentities.TileEntityRoutingPipe;
import ihamfp.exppipes.tileentities.pipeconfig.ConfigRoutingPipe;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;

public class ContainerPipeConfig extends ContainerTileEntity<TileEntityRoutingPipe> {
	public ContainerPipeConfig(IInventory playerInventory, TileEntityRoutingPipe te, ConfigRoutingPipe conf) {
		super(playerInventory, te);
		
		if (!te.getWorld().isRemote) {
			IBlockState currentState = te.getWorld().getBlockState(te.getPos());
			te.getWorld().notifyBlockUpdate(te.getPos(), currentState, currentState, 2);
		}

		this.addOwnSlots(conf);
		this.addPlayerSlots(playerInventory, 8, 86);
		if (conf == null) {
			ExppipesMod.logger.error("No associated config found - creating empty container");
		}
	}
	
	protected void addOwnSlots(ConfigRoutingPipe conf) {
		if (this.te.upgradesItemHandler != null) {
			for (int i=0; i<Integer.min(this.te.upgradesItemHandler.getSlots(), 4); i++) {
				int ix = -36+((i%2)*18);
				int iy = 18+((i/2)*18);
				this.ownSlots.put(this.inventorySlots.size(), this.addSlotToContainer(new SlotItemHandler(this.te.upgradesItemHandler, i, ix, iy)));
			}
		}
	}
	
	@Override
	public ItemStack transferStackInSlot(EntityPlayer playerIn, int index) {
		Slot slot = this.inventorySlots.get(index);
        if (slot == null || !slot.getHasStack()) return ItemStack.EMPTY;
        ItemStack stack = slot.getStack();
        if (stack.getItem() instanceof ItemUpgrade && this.te.upgradesItemHandler != null) { // add upgrade
        	return super.transferStackInSlot(playerIn, index);
        } else { // add config
        	return ItemStack.EMPTY;
        }
	}
}
