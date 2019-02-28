package ihamfp.exppipes.blocks;

import ihamfp.exppipes.tileentities.TileEntityProviderPipe;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class BlockProviderPipe extends BlockRoutingPipe {
	public BlockProviderPipe(String registryID) {
		super(registryID);
	}
	
	@Override
	public TileEntity createTileEntity(World worldIn, IBlockState state) {
		return new TileEntityProviderPipe();
	}
}
