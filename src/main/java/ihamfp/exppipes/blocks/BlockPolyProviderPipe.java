package ihamfp.exppipes.blocks;

import ihamfp.exppipes.items.ItemUpgrade;
import ihamfp.exppipes.tileentities.TileEntityPolyProviderPipe;
import ihamfp.exppipes.tileentities.TileEntityRoutingPipe;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.items.IItemHandler;

public class BlockPolyProviderPipe extends BlockRoutingPipe {
	public BlockPolyProviderPipe(String registryID) {
		super(registryID);
	}
	
	@Override
	public TileEntity createTileEntity(World worldIn, IBlockState state) {
		return new TileEntityPolyProviderPipe();
	}

	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		ItemStack heldStack = playerIn.getHeldItem(hand);
		if (!worldIn.isRemote && heldStack.getItem() instanceof ItemUpgrade) {
			TileEntity te = worldIn.getTileEntity(pos);
			if (te != null && te instanceof TileEntityRoutingPipe) {
				IItemHandler upgrades = ((TileEntityRoutingPipe)te).upgradesItemHandler;
				if (upgrades != null) {
					ItemStack singleUpgrade = heldStack.copy();
					singleUpgrade.setCount(1);
					for (int i=0; i<upgrades.getSlots(); i++) {
						if (upgrades.insertItem(i, singleUpgrade, false).isEmpty()) {
							heldStack.shrink(1);
							return true;
						}
					}
				}
			}
		}
		return super.onBlockActivated(worldIn, pos, state, playerIn, hand, facing, hitX, hitY, hitZ);
	}
}
