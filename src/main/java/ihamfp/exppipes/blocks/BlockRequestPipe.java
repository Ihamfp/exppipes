package ihamfp.exppipes.blocks;

import ihamfp.exppipes.ExppipesMod;
import ihamfp.exppipes.tileentities.TileEntityRequestPipe;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
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
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		//ExppipesMod.logger.info("Clicked on " + this.getLocalizedName() + " at " + pos.toString());
		if (worldIn.isRemote) {
			return true;
		} else if (!(worldIn.getTileEntity(pos) instanceof TileEntityRequestPipe)) {
			return false;
		}
		
		if (!playerIn.isSneaking()) {
			playerIn.openGui(ExppipesMod.instance, 3, worldIn, pos.getX(), pos.getY(), pos.getZ());
			return true;
		}
		
		return super.onBlockActivated(worldIn, pos, state, playerIn, hand, facing, hitX, hitY, hitZ);
	}
}
