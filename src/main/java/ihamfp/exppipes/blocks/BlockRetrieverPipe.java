package ihamfp.exppipes.blocks;

import ihamfp.exppipes.tileentities.TileEntityRetrieverPipe;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class BlockRetrieverPipe extends BlockSupplierPipe {
	public BlockRetrieverPipe(String registryID) {
		super(registryID);
	}
	
	@Override
	public TileEntity createTileEntity(World worldIn, IBlockState state) {
		return new TileEntityRetrieverPipe();
	}
}
