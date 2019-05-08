package ihamfp.exppipes.blocks;

import ihamfp.exppipes.tileentities.TileEntitySupplierPipe;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class BlockSupplierPipe extends BlockRoutingPipe {
	public BlockSupplierPipe(String registryID) {
		super(registryID);
	}
	
	@Override
	public TileEntity createTileEntity(World worldIn, IBlockState state) {
		return new TileEntitySupplierPipe();
	}
	
	@Override
	public int getGuiID() {
		return 2;
	}
}
