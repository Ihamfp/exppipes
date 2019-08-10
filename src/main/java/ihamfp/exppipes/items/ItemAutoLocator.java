package ihamfp.exppipes.items;

import ihamfp.exppipes.pipenetwork.BlockDimPos;
import ihamfp.exppipes.tileentities.TileEntityRoutingPipe;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.nbt.NBTTagList;

public class ItemAutoLocator extends ItemLocator {

	public ItemAutoLocator(String id) {
		super(id);
	}
	
	@Override
	protected void add2mem(TileEntityRoutingPipe te, NBTTagList positions) {
		if (te.network == null) return;
		for (TileEntityRoutingPipe node : te.network.nodes) {
			if (node == te) continue;
			if (node.sinkConfig.filters.size() > 0 && node.sinkConfig.equals(te.sinkConfig)) {
				BlockDimPos tePos = new BlockDimPos(node);
				positions.appendTag(new NBTTagIntArray(new int[] {tePos.getX(), tePos.getY(), tePos.getZ(), tePos.dimension}));
			}
		}
		super.add2mem(te, positions);
	}

}
