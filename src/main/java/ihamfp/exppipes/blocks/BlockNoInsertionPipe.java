package ihamfp.exppipes.blocks;

import ihamfp.exppipes.tileentities.TileEntityNoInsertionPipe;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class BlockNoInsertionPipe extends BlockPipe {

	public BlockNoInsertionPipe(String registryID) {
		super(registryID);
	}
	
	@Override
	public TileEntity createTileEntity(World worldIn, IBlockState state) {
		return new TileEntityNoInsertionPipe();
	}
}
