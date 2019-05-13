package ihamfp.exppipes.blocks;

import ihamfp.exppipes.tileentities.TileEntityStockKeeperPipe;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class BlockStockKeeperPipe extends BlockRoutingPipe {
	public BlockStockKeeperPipe(String registryID) {
		super(registryID);
	}
	
	@Override
	public TileEntity createTileEntity(World worldIn, IBlockState state) {
		return new TileEntityStockKeeperPipe();
	}
	
	@Override
	public int getGuiID() {
		return 6;
	}
}
