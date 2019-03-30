package ihamfp.exppipes.blocks;

import ihamfp.exppipes.tileentities.TileEntityCraftingPipe;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class BlockCraftingPipe extends BlockRoutingPipe {
	public BlockCraftingPipe(String registryID) {
		super(registryID);
	}
	
	@Override
	public TileEntity createTileEntity(World worldIn, IBlockState state) {
		return new TileEntityCraftingPipe();
	}
}
