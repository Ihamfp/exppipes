package ihamfp.exppipes.blocks;

import ihamfp.exppipes.tileentities.TileEntityExtractionPipe;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class BlockExtractionPipe extends BlockRoutingPipe {
	public BlockExtractionPipe(String registryID) {
		super(registryID);
	}
	
	@Override
	public TileEntity createTileEntity(World world, IBlockState state) {
		return new TileEntityExtractionPipe();
	}
	
	@Override
	public int getGuiID() {
		return 5;
	}
}
