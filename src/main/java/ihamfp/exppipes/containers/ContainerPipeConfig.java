package ihamfp.exppipes.containers;

import ihamfp.exppipes.ExppipesMod;
import ihamfp.exppipes.containers.slot.ConfigSlot;
import ihamfp.exppipes.tileentities.TileEntityRoutingPipe;
import ihamfp.exppipes.tileentities.pipeconfig.ConfigRoutingPipe;
import net.minecraft.block.state.IBlockState;
import net.minecraft.inventory.IInventory;

public class ContainerPipeConfig extends ContainerBase<TileEntityRoutingPipe> {
	public ContainerPipeConfig(IInventory playerInventory, TileEntityRoutingPipe te) {
		this(playerInventory, te, te.sinkConfig);
	}
	
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
		for (int i=0; i<9;i++) {
			this.ownSlots.put(this.inventorySlots.size(), this.addSlotToContainer(new ConfigSlot(conf, i, 8+i*18, 18)));
		}
	}
}
