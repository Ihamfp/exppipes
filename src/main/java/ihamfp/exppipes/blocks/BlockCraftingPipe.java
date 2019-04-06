package ihamfp.exppipes.blocks;

import ihamfp.exppipes.ExppipesMod;
import ihamfp.exppipes.tileentities.TileEntityCraftingPipe;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockCraftingPipe extends BlockRoutingPipe {
	public BlockCraftingPipe(String registryID) {
		super(registryID);
	}
	
	@Override
	public TileEntity createTileEntity(World worldIn, IBlockState state) {
		return new TileEntityCraftingPipe();
	}
	
	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		//ExppipesMod.logger.info("Clicked on " + this.getLocalizedName() + " at " + pos.toString());
		if (worldIn.isRemote) {
			return true;
		} else if (!(worldIn.getTileEntity(pos) instanceof TileEntityCraftingPipe)) {
			return false;
		}
		
		if (!playerIn.isSneaking()) {
			playerIn.openGui(ExppipesMod.instance, 4, worldIn, pos.getX(), pos.getY(), pos.getZ());
			return true;
		}
		
		return super.onBlockActivated(worldIn, pos, state, playerIn, hand, facing, hitX, hitY, hitZ);
	}
}
