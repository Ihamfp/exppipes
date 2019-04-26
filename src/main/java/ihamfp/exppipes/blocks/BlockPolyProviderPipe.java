package ihamfp.exppipes.blocks;

import ihamfp.exppipes.tileentities.TileEntityPolyProviderPipe;
import ihamfp.exppipes.tileentities.TileEntityProviderPipe;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class BlockPolyProviderPipe extends BlockRoutingPipe {
	public BlockPolyProviderPipe(String registryID) {
		super(registryID);
	}
	
	@Override
	public TileEntity createTileEntity(World worldIn, IBlockState state) {
		return new TileEntityPolyProviderPipe();
	}
}
