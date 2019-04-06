package ihamfp.exppipes.containers;

import ihamfp.exppipes.tileentities.TileEntityRequestPipe;
import net.minecraft.inventory.IInventory;

public class ContainerPipeRequest extends ContainerTileEntity<TileEntityRequestPipe> {

	public ContainerPipeRequest(IInventory playerInventory, TileEntityRequestPipe te) {
		super(playerInventory, te);
		this.addPlayerSlots(playerInventory, 8, 149);
	}

}
