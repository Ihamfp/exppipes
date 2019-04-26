package ihamfp.exppipes.blocks;

import ihamfp.exppipes.tileentities.TileEntityCountingPipe;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;

public class BlockCountingPipe extends BlockPipe {
	public BlockCountingPipe(String registryID) {
		super(registryID);
	}
	
	@Override
	public TileEntity createTileEntity(World world, IBlockState state) {
		return new TileEntityCountingPipe();
	}
	
	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		if (!playerIn.isSneaking()) {
			if (worldIn.isRemote) return true;
			TileEntity te = worldIn.getTileEntity(pos);
			if (te instanceof TileEntityCountingPipe) {
				playerIn.sendMessage(new TextComponentString("Count: " + Integer.toString(((TileEntityCountingPipe)te).count)));
			}
		}
		return super.onBlockActivated(worldIn, pos, state, playerIn, hand, facing, hitX, hitY, hitZ);
	}
}
