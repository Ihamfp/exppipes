package ihamfp.exppipes.tileentities;

import ihamfp.exppipes.pipenetwork.PipeNetwork;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

/**
 * For anything connected to the network, but not part of it
 */
public class TileEntityNetworkBlock extends TileEntity {
	public PipeNetwork searchNetwork() {
		for (EnumFacing f : EnumFacing.VALUES) {
			TileEntity te = this.world.getTileEntity(this.pos.offset(f));
			if (te != null && te instanceof TileEntityRoutingPipe && ((TileEntityRoutingPipe)te).network != null) {
				return ((TileEntityRoutingPipe)te).network;
			}
		}
		return null;
	}
}
