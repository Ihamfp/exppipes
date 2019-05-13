package ihamfp.exppipes.blocks;

import ihamfp.exppipes.tileentities.TileEntityBufferStackDisplay;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.items.ItemStackHandler;

public class BlockBufferStackDisplay extends BlockStackDisplay {
	public BlockBufferStackDisplay(String registryID) {
		super(registryID);
	}
	
	@Override
	public TileEntity createTileEntity(World world, IBlockState state) {
		return new TileEntityBufferStackDisplay();
	}
	
	@Override
	public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
		TileEntity te = worldIn.getTileEntity(pos);
		if (te == null || te.isInvalid() || !(te instanceof TileEntityBufferStackDisplay)) {
			super.breakBlock(worldIn, pos, state);
			return;
		}
		TileEntityBufferStackDisplay tesd = (TileEntityBufferStackDisplay)te;
		for (int i=0; i<tesd.bufferStackHandler.getSlots();i++) {
			if (!tesd.bufferStackHandler.getStackInSlot(i).isEmpty()) {
				worldIn.spawnEntity(new EntityItem(worldIn, pos.getX(), pos.getY(), pos.getZ(), tesd.bufferStackHandler.getStackInSlot(i)));
			}
		}
		
		super.breakBlock(worldIn, pos, state);
	}
	
	@Override
	public void onBlockClicked(World worldIn, BlockPos pos, EntityPlayer playerIn) {
		if (worldIn.isRemote) {
			super.onBlockClicked(worldIn, pos, playerIn);
			return;
		}
		RayTraceResult rayRes = ForgeHooks.rayTraceEyes(playerIn, playerIn.getEntityAttribute(EntityPlayer.REACH_DISTANCE).getAttributeValue());
		IBlockState state = worldIn.getBlockState(pos);
		if (this.getFrontFace(state) != rayRes.sideHit) {
			super.onBlockClicked(worldIn, pos, playerIn);
			return;
		}
		
		TileEntity te = worldIn.getTileEntity(pos);
		if (te == null || !(te instanceof TileEntityBufferStackDisplay)) super.onBlockClicked(worldIn, pos, playerIn);
		ItemStackHandler ish = ((TileEntityBufferStackDisplay)te).bufferStackHandler;
		ItemStack stack = null;
		for (int i=0; i<ish.getSlots();i++) {
			int maxGetCount = playerIn.isSneaking()?ish.getSlotLimit(i):1;
			stack = ish.extractItem(i, maxGetCount, false);
			if (stack.isEmpty()) continue;
			if (!playerIn.inventory.addItemStackToInventory(stack)) {
				BlockPos stackPos = pos.offset(this.getFrontFace(state));
				worldIn.spawnEntity(new EntityItem(worldIn, stackPos.getX()+0.5, stackPos.getY()+0.5, stackPos.getZ()+0.5, stack));
			}
			break;
		}
		
		super.onBlockClicked(worldIn, pos, playerIn);
	}
}
