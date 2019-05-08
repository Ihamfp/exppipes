package ihamfp.exppipes.blocks;

import ihamfp.exppipes.tileentities.TileEntityRequestPipe;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class BlockRequestPipe extends BlockRoutingPipe {

	public BlockRequestPipe(String registryID) {
		super(registryID);
	}
	
	@Override
	public TileEntity createTileEntity(World worldIn, IBlockState state) {
		return new TileEntityRequestPipe();
	}
	
	@Override
	public int getGuiID() {
		return 3;
	}
}
