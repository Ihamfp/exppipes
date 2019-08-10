package ihamfp.exppipes.items;

import java.util.ArrayList;
import java.util.List;

import ihamfp.exppipes.ExppipesMod;
import ihamfp.exppipes.pipenetwork.BlockDimPos;
import ihamfp.exppipes.tileentities.TileEntityRobinPipe;
import ihamfp.exppipes.tileentities.TileEntityRoutingPipe;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants.NBT;

public class ItemLocator extends Item {
	public ItemLocator(String id) {
		this.setRegistryName(ExppipesMod.MODID, id);
		this.setMaxStackSize(1);
	}
	
	protected void add2mem(TileEntityRoutingPipe te, NBTTagList positions) {
		BlockDimPos tePos = new BlockDimPos(te);
		positions.appendTag(new NBTTagIntArray(new int[] {tePos.getX(), tePos.getY(), tePos.getZ(), tePos.dimension}));
	}
	
	@Override
	public EnumActionResult onItemUseFirst(EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, EnumHand hand) {
		if (!world.isRemote) {
			ItemStack heldStack = player.getHeldItem(hand);
			NBTTagCompound heldNBT = heldStack.hasTagCompound()?heldStack.getTagCompound():(new NBTTagCompound());
			NBTTagList positions = heldNBT.hasKey("positions")?heldNBT.getTagList("positions", NBT.TAG_INT_ARRAY):(new NBTTagList());
			TileEntity te = world.getTileEntity(pos);
			if (te instanceof TileEntityRobinPipe) {
				ArrayList<BlockDimPos> robinList = ((TileEntityRobinPipe)te).robinList;
				robinList.clear();
				for (int i=0; i<positions.tagCount(); i++) {
					int pipePos[] = ((NBTTagIntArray) positions.get(i)).getIntArray();
					robinList.add(new BlockDimPos(pipePos[0], pipePos[1], pipePos[2], pipePos[3]));
				}
				player.sendMessage(new TextComponentString("Saved to pipe!"));
				return EnumActionResult.SUCCESS;
			} else if (te instanceof TileEntityRoutingPipe) {
				this.add2mem((TileEntityRoutingPipe)te, positions);
				heldNBT.setTag("positions", positions);
				heldStack.setTagCompound(heldNBT);
				te.markDirty();
				player.sendMessage(new TextComponentString("Added to memory!"));
				return EnumActionResult.SUCCESS;
			}
		}
		return super.onItemUseFirst(player, world, pos, side, hitX, hitY, hitZ, hand);
	}
	
	@Override
	public void addInformation(ItemStack stack, World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
		NBTTagCompound heldNBT = stack.hasTagCompound()?stack.getTagCompound():(new NBTTagCompound());
		tooltip.add("Saved positions:");
		NBTTagList positions = heldNBT.hasKey("positions")?heldNBT.getTagList("positions", NBT.TAG_INT_ARRAY):(new NBTTagList());
		for (int i=0; i<positions.tagCount(); i++) {
			int pos[] = ((NBTTagIntArray) positions.get(i)).getIntArray();
			tooltip.add(" * X:" + pos[0] + "; Y:" + pos[1] + "; Z:" + pos[2] + "; dimension: " + pos[3]);
		}
		super.addInformation(stack, worldIn, tooltip, flagIn);
	}
	
	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
		if (!worldIn.isRemote) {
			ItemStack heldStack = playerIn.getHeldItem(handIn);
			NBTTagCompound heldNBT = heldStack.hasTagCompound()?heldStack.getTagCompound():(new NBTTagCompound());
			if (playerIn.isSneaking()) {
				heldNBT.setTag("positions", new NBTTagList());
				heldStack.setTagCompound(heldNBT);
				playerIn.sendMessage(new TextComponentString("Cleared memory!"));
			}
		}
		return super.onItemRightClick(worldIn, playerIn, handIn);
	}
}
