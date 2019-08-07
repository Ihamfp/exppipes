package ihamfp.exppipes.blocks;

import ihamfp.exppipes.tileentities.TileEntityRobinPipe;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class BlockRobinPipe extends BlockRoutingPipe {

	public BlockRobinPipe(String registryID) {
		super(registryID);
	}
	
	@Override
	public TileEntity createTileEntity(World worldIn, IBlockState state) {
		return new TileEntityRobinPipe();
	}
	
	/*@Override
	public int getGuiID() {
		return 1;
	}*/

}
