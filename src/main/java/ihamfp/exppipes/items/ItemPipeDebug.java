package ihamfp.exppipes.items;

import ihamfp.exppipes.ExppipesMod;
import ihamfp.exppipes.tileentities.TileEntityRoutingPipe;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;

public class ItemPipeDebug extends Item {
	public ItemPipeDebug(String id) {
		this.setRegistryName(ExppipesMod.MODID, id);
		this.setMaxStackSize(1);
	}

	@Override
	public EnumActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		if (!worldIn.isRemote) {
			TileEntity te = worldIn.getTileEntity(pos);
			if (te instanceof TileEntityRoutingPipe) {
				TileEntityRoutingPipe terp = (TileEntityRoutingPipe)te;
				player.sendMessage(new TextComponentString("Connected nodes:"));
				for (EnumFacing f : EnumFacing.VALUES) {
					TileEntityRoutingPipe cnode = terp.connectedNodes.get(f);
					if (cnode == null) continue;
					player.sendMessage(new TextComponentString(f.getName() + ": " + cnode.toString()));
				}
			}
		}
		return super.onItemUse(player, worldIn, pos, hand, facing, hitX, hitY, hitZ);
	}
}
